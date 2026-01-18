package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.JobFitEvaluationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApplicationResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.Application;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.JobPosting;
import com.samjay.AI_Powered.Job.Application.Tracker.globalexception.CandidateNotFoundException;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ApplicationRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.CandidateRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.JobPostingRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.ApplicationService;
import com.samjay.AI_Powered.Job.Application.Tracker.services.JobPostingService;
import com.samjay.AI_Powered.Job.Application.Tracker.services.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class ApplicationServiceImplementation implements ApplicationService {

    @Value("classpath:/prompts/AiRecommendationSystemMessage.st")
    private Resource aiRecommendationSystemMessage;

    @Value("classpath:/prompts/AiRecommendationUserMessage.st")
    private Resource aiRecommendationUserMessage;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    private final JobPostingService jobPostingService;

    private final CandidateRepository candidateRepository;

    private final ApplicationRepository applicationRepository;

    private final JobPostingRepository jobPostingRepository;

    private final ReminderService reminderService;

    private static final String DEFAULT_ERROR_MESSAGE = "I apologize, but I encountered an error while generating recommendations. Please try again.";

    @Override
    public Flux<String> getAiRecommendationForJob(Mono<JobFitEvaluationRequestDto> jobFitEvaluationRequestDtoMono) {

        long overallStartTime = System.currentTimeMillis();

        log.info("[OVERALL] Starting AI recommendation process");

        return jobFitEvaluationRequestDtoMono.flatMapMany(jobFitEvaluationRequestDto -> {

            log.info("[OVERALL] Processing request for job URL: {}", jobFitEvaluationRequestDto.getJobUrl());

            return ReactiveSecurityContextHolder.getContext().flatMapMany(securityContext -> {

                String email = securityContext.getAuthentication().getName();

                String cacheKey = CACHE_KEY_JOB_URL + jobFitEvaluationRequestDto.getJobUrl();

                log.info("[I/O - DB] Starting candidate lookup for email: {}", email);

                long candidateLookupStart = System.currentTimeMillis();

                return candidateRepository.findByEmail(email)
                        .doOnSuccess(candidate -> {

                            long duration = System.currentTimeMillis() - candidateLookupStart;

                            log.info("[I/O - DB] Candidate lookup completed in {}ms for email: {}", duration, email);

                        })
                        .flatMapMany(candidate -> {

                            log.info("[I/O - REDIS] Starting Redis cache check for key: {}", cacheKey);

                            long cacheCheckStart = System.currentTimeMillis();

                            return reactiveRedisOperations.opsForValue().get(cacheKey)
                                    .doOnSuccess(obj -> {

                                        long duration = System.currentTimeMillis() - cacheCheckStart;

                                        if (obj != null) {

                                            log.info("[I/O - REDIS] Cache check completed in {}ms - CACHE HIT for key: {}", duration, cacheKey);

                                        } else {

                                            log.info("[I/O - REDIS] Cache check completed in {}ms - CACHE MISS for key: {}", duration, cacheKey);

                                        }
                                    })
                                    .flatMap(obj -> {

                                        if (obj instanceof JobPosting cachedJobPosting) {

                                            log.info("Cache hit for job URL: {}", jobFitEvaluationRequestDto.getJobUrl());

                                            return Mono.just(cachedJobPosting);

                                        }

                                        log.warn("Invalid cache entry for job URL: {}, type: {}", jobFitEvaluationRequestDto.getJobUrl(), obj != null ? obj.getClass().getName() : "null");

                                        return Mono.empty();

                                    })
                                    .switchIfEmpty(Mono.defer(() -> {

                                        log.info("[I/O - COMBINED] Cache miss - starting job posting fetch and cache write");

                                        long fetchAndCacheStart = System.currentTimeMillis();

                                        return jobPostingService.fetchJobDetailsFromAiAsync(jobFitEvaluationRequestDto.getJobUrl(), candidate.getId())
                                                .flatMap(jobPosting -> {

                                                    log.info("[I/O - REDIS] Starting Redis cache write for key: {}", cacheKey);

                                                    long cacheWriteStart = System.currentTimeMillis();

                                                    return reactiveRedisOperations.opsForValue()
                                                            .set(cacheKey, jobPosting, CACHE_TTL)
                                                            .doOnSuccess(success -> {

                                                                long cacheWriteDuration = System.currentTimeMillis() - cacheWriteStart;

                                                                long totalDuration = System.currentTimeMillis() - fetchAndCacheStart;

                                                                if (Boolean.TRUE.equals(success)) {

                                                                    log.info("[I/O - REDIS] Cache write completed in {}ms for URL: {}", cacheWriteDuration, jobFitEvaluationRequestDto.getJobUrl());

                                                                    log.info("[I/O - COMBINED] Total fetch + cache time: {}ms", totalDuration);

                                                                } else {

                                                                    log.warn("[I/O - REDIS] Failed to cache job posting after {}ms for URL: {}", cacheWriteDuration, jobFitEvaluationRequestDto.getJobUrl());

                                                                }
                                                            })
                                                            .thenReturn(jobPosting)
                                                            .onErrorResume(e -> {

                                                                long duration = System.currentTimeMillis() - cacheWriteStart;

                                                                log.error("[I/O - REDIS] Cache write failed after {}ms, continuing without cache", duration, e);

                                                                return Mono.just(jobPosting);
                                                            });
                                                });
                                    }))
                                    .onErrorResume(e -> {

                                        long duration = System.currentTimeMillis() - cacheCheckStart;

                                        log.error("[I/O - REDIS] Redis error after {}ms for cache key: {}, falling back to direct fetch", duration, cacheKey, e);

                                        return jobPostingService.fetchJobDetailsFromAiAsync(jobFitEvaluationRequestDto.getJobUrl(), candidate.getId());

                                    })
                                    .flatMapMany(jobPosting -> {

                                        long streamSetupStart = System.currentTimeMillis();

                                        log.info("[I/O - AI-STREAM] Starting AI streaming response setup");

                                        return streamResponseFromAi(candidate.getId(), jobFitEvaluationRequestDto.getResumeId(), jobPosting)
                                                .doOnSubscribe(s -> {

                                                    long duration = System.currentTimeMillis() - streamSetupStart;

                                                    log.info("[I/O - AI-STREAM] Stream setup completed in {}ms", duration);

                                                })
                                                .doOnComplete(() -> {

                                                    long totalDuration = System.currentTimeMillis() - overallStartTime;

                                                    log.info("[OVERALL] Total AI recommendation process completed in {}ms", totalDuration);

                                                });
                                    });
                        })
                        .switchIfEmpty(Flux.error(new CandidateNotFoundException(email)));
            });
        });
    }

    @Override
    public Mono<ApiResponse<List<ApplicationResponseDto>>> getAllApplicationsWithAiSuggestions() {

        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {

            String email = securityContext.getAuthentication().getName();

            return candidateRepository.findByEmail(email)
                    .flatMap(candidate -> {

                        String cacheKey = APPLICATION_CACHE_KEY + candidate.getId();

                        return reactiveRedisOperations.opsForValue().get(cacheKey)
                                .flatMap(obj -> {

                                    if (obj instanceof List<?> cachedAiSuggestions && !cachedAiSuggestions.isEmpty() && cachedAiSuggestions.getFirst() instanceof ApplicationResponseDto) {

                                        log.info("Cache hit for AI suggestions for candidate email: {}", email);

                                        List<ApplicationResponseDto> applicationResponseDtos = (List<ApplicationResponseDto>) cachedAiSuggestions;

                                        return Mono.just(ApiResponse.success("Applications retrieved successfully", applicationResponseDtos));
                                    } else {

                                        log.warn("Cache miss for AI suggestions for candidate email: {}, type: {}", email, obj != null ? obj.getClass().getName() : "null");

                                        return Mono.empty();
                                    }
                                })
                                .onErrorResume(error -> {

                                    log.error("Error accessing Redis cache for candidate email: {}", email, error);

                                    return Mono.empty();
                                })
                                .switchIfEmpty(applicationRepository.findAllByCandidateId(candidate.getId())
                                        .flatMap(application -> jobPostingRepository.findById(application.getJobPostingId())
                                                .map(jobPosting -> ApplicationResponseDto.builder()
                                                        .jobUrl(jobPosting.getJobUrl())
                                                        .aiSuggestions(application.getAiSuggestions())
                                                        .build())
                                        )
                                        .collectList()
                                        .flatMap(applicationResponseDtos -> reactiveRedisOperations.opsForValue().set(cacheKey, applicationResponseDtos, CACHE_TTL)
                                                .doOnSuccess(result -> log.info("Cached AI suggestions for candidate email: {} with key: {}", email, cacheKey))
                                                .thenReturn(ApiResponse.success("Applications retrieved successfully", applicationResponseDtos))
                                                .onErrorReturn(ApiResponse.success("Applications retrieved successfully", applicationResponseDtos))
                                        )
                                );

                    })
                    .switchIfEmpty(Mono.just(ApiResponse.error("Candidate not found")))
                    .onErrorResume(error -> {

                        log.error("Error fetching applications for candidate email: {}", email, error);

                        return Mono.just(ApiResponse.error("An error occurred while fetching applications"));
                    });
        });
    }


    private Flux<String> streamResponseFromAi(String candidateId, String resumeId, JobPosting jobPosting) {

        long startTime = System.currentTimeMillis();

        log.info("[I/O - VECTOR] Starting vector store setup and AI streaming for candidateId: {}, resumeId: {}", candidateId, resumeId);

        try {

            long vectorSetupStart = System.currentTimeMillis();

            FilterExpressionBuilder builder = new FilterExpressionBuilder();

            DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .filterExpression(builder.and(builder.eq(CANDIDATE_ID, candidateId),
                                    builder.eq(RESUME_ID, resumeId))
                            .build())
                    .build();

            var contextualQueryAugmenter = ContextualQueryAugmenter
                    .builder()
                    .allowEmptyContext(true)
                    .build();

            RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor
                    .builder()
                    .documentRetriever(documentRetriever)
                    .documentJoiner(new ConcatenationDocumentJoiner())
                    .queryAugmenter(contextualQueryAugmenter)
                    .build();

            long vectorSetupDuration = System.currentTimeMillis() - vectorSetupStart;

            log.info("[I/O - VECTOR] Vector store and RAG pipeline setup completed in {}ms", vectorSetupDuration);

            log.info("[I/O - AI-STREAM] Starting AI streaming call with RAG");

            long streamStart = System.currentTimeMillis();

            StringBuilder aiResponseBuffer = new StringBuilder();

            return chatClient.prompt()
                    .advisors(retrievalAugmentationAdvisor)
                    .system(system -> system.text(aiRecommendationSystemMessage))
                    .user(user -> user.text(aiRecommendationUserMessage).params(Map.of("jobTitle", jobPosting.getTitle(),
                            "jobDescription", jobPosting.getDescription(),
                            "jobLocation", jobPosting.getLocation())))
                    .stream()
                    .content()
                    .doOnSubscribe(s -> {

                        long setupDuration = System.currentTimeMillis() - streamStart;

                        log.info("[I/O - AI-STREAM] Stream subscribed after {}ms", setupDuration);

                    })
                    .doOnNext(aiResponseBuffer::append)
                    .concatWith(Mono.defer(() -> {

                                long totalStreamDuration = System.currentTimeMillis() - streamStart;

                                long totalDuration = System.currentTimeMillis() - startTime;

                                log.info("[I/O - AI-STREAM] Stream completed in {}ms", totalStreamDuration);

                                log.info("[I/O - VECTOR+AI] Total vector + streaming time: {}ms", totalDuration);

                                return persistAiSuggestionsAndCreateReminder(candidateId, jobPosting, resumeId, aiResponseBuffer.toString())
                                        .doOnSuccess(v -> log.info("AI suggestions persisted for candidateId={}, jobPostingId={}", candidateId, jobPosting.getId()))
                                        .doOnError(e -> log.error("Failed to persist AI suggestions for candidateId={}, jobPostingId={}", candidateId, jobPosting.getId(), e));
                            })
                            .thenMany(Flux.empty()))
                    .onErrorResume(e -> {

                        long duration = System.currentTimeMillis() - streamStart;

                        log.error("[I/O - AI-STREAM] Error after {}ms streaming AI response", duration, e);

                        return Flux.just("Error generating recommendations. Please try again.");

                    });
        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - startTime;

            log.error("[I/O - VECTOR] Error after {}ms setting up RAG pipeline", duration, ex);

            return Flux.just(DEFAULT_ERROR_MESSAGE);

        }
    }

    private Mono<Void> persistAiSuggestionsAndCreateReminder(String candidateId, JobPosting jobPosting, String resumeId, String aiSuggestions) {

        return Mono.defer(() -> {

            Application application = Application.builder()
                    .id(UUID.randomUUID().toString())
                    .candidateId(candidateId)
                    .jobPostingId(jobPosting.getId())
                    .resumeId(resumeId)
                    .aiSuggestions(aiSuggestions)
                    .build();

            return applicationRepository.save(application)
                    .doOnSuccess(app -> log.info("Saved AI suggestions for applicationId: {}", app.getId()))
                    .flatMap(savedApplication -> {

                        LocalDateTime deadline = jobPosting.getDeadline();

                        if (deadline == null || deadline.toLocalDate().isBefore(LocalDate.now())) {

                            return Mono.empty();

                        }

                        LocalDate reminderDate = deadline.toLocalDate().minusDays(2);

                        if (reminderDate.isBefore(LocalDate.now().minusDays(1))) {

                            return Mono.empty();

                        }

                        return reminderService.createReminder(savedApplication.getId(), reminderDate);
                    });
        });
    }
}