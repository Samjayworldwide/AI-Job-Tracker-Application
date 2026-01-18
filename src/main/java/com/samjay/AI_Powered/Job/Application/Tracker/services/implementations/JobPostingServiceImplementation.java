package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.JobPostingRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.JobPostingResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.JobPosting;
import com.samjay.AI_Powered.Job.Application.Tracker.globalexception.AiExtractionException;
import com.samjay.AI_Powered.Job.Application.Tracker.globalexception.JobPageFetchException;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.CandidateRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.JobPostingRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.JobPostingService;
import com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.JOB_POSTING_CACHE_KEY;


@Service
@Slf4j
@SuppressWarnings("unchecked")
public class JobPostingServiceImplementation implements JobPostingService {

    @Value("classpath:/prompts/JobPostingSystemMessage.st")
    private Resource jobPostingSystemMessage;

    @Value("classpath:/prompts/JobPostingUserMessage.st")
    private Resource jobPostingUserMessage;

    private final ChatClient chatClient;

    private final WebClient webClient;

    private final JobPostingRepository jobPostingRepository;

    private final CandidateRepository candidateRepository;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    public JobPostingServiceImplementation(ChatClient chatClient, WebClient.Builder webClientBuilder,
                                           JobPostingRepository jobPostingRepository,
                                           CandidateRepository candidateRepository,
                                           ReactiveRedisOperations<String, Object> reactiveRedisOperations) {

        this.chatClient = chatClient;

        this.webClient = webClientBuilder.build();

        this.jobPostingRepository = jobPostingRepository;

        this.candidateRepository = candidateRepository;

        this.reactiveRedisOperations = reactiveRedisOperations;
    }

    @Override
    public Mono<JobPosting> fetchJobDetailsFromAiAsync(String jobUrl, String candidateId) {

        long startTime = System.currentTimeMillis();

        log.info("[OVERALL] Starting job details fetch for URL: {}, candidateId: {}", jobUrl, candidateId);

        return fetchJobPage(jobUrl)
                .publishOn(Schedulers.boundedElastic())
                .map(this::extractReadableText)
                .flatMap(this::callAiServiceForJobDetailsAsync)
                .doOnSuccess(jobPosting -> log.info("AI extracted job details: {}", Utility.convertObjectToString(jobPosting)))
                .map(dto -> buildJobPosting(candidateId, jobUrl, dto))
                .flatMap(jobPostingRepository::save)
                .flatMap(savedJobPosting -> invalidateJobPostingsCache(candidateId).thenReturn(savedJobPosting))
                .doOnSuccess(result -> {

                    long duration = System.currentTimeMillis() - startTime;

                    log.info("[OVERALL] Total time for fetchJobDetailsFromAiAsync: {}ms for URL: {}", duration, jobUrl);

                })
                .doOnError(e -> {

                    long duration = System.currentTimeMillis() - startTime;

                    log.error("[OVERALL] Failed after {}ms to fetch job details from URL: {} for candidate: {}", duration, jobUrl, candidateId, e);

                });
    }

    @Override
    public Mono<ApiResponse<List<JobPostingResponseDto>>> getAllJobPostingsForCandidate() {

        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {

            String email = securityContext.getAuthentication().getName();

            return candidateRepository.findByEmail(email)
                    .flatMap(candidate -> {

                        String cacheKey = JOB_POSTING_CACHE_KEY + candidate.getId();

                        return reactiveRedisOperations.opsForValue().get(cacheKey)
                                .flatMap(obj -> {

                                    if (obj instanceof List<?> cachedJobPostings && !cachedJobPostings.isEmpty() && cachedJobPostings.getFirst() instanceof JobPostingResponseDto) {

                                        List<JobPostingResponseDto> jobPostings = (List<JobPostingResponseDto>) cachedJobPostings;

                                        log.info("Fetched job postings from cache for candidate: {}", email);

                                        return Mono.just(ApiResponse.success("Job postings retrieved successfully", jobPostings));

                                    } else {

                                        log.info("No cached job postings found for candidate: {}, fetching from database", email);

                                        return Mono.empty();
                                    }
                                })
                                .onErrorResume(error -> {

                                    log.info("An unexpected error occurred fetching from redis for candidate: {}", email);

                                    return Mono.empty();

                                })
                                .switchIfEmpty(jobPostingRepository.findAllByCandidateId(candidate.getId())
                                        .map(jobPosting -> JobPostingResponseDto.builder()
                                                .title(jobPosting.getTitle())
                                                .company(jobPosting.getCompany())
                                                .location(jobPosting.getLocation())
                                                .jobUrl(jobPosting.getJobUrl())
                                                .description(jobPosting.getDescription())
                                                .deadline(jobPosting.getDeadline())
                                                .build()
                                        )
                                        .collectList()
                                        .flatMap(jobPostingResponseDtos -> reactiveRedisOperations.opsForValue()
                                                .set(cacheKey, jobPostingResponseDtos, Utility.CACHE_TTL)
                                                .doOnError(error -> log.error("An unexpected error occurred saving to redis for candidate: {}: {}", email, error.getMessage()))
                                                .thenReturn(ApiResponse.success("Job postings retrieved successfully", jobPostingResponseDtos))
                                                .onErrorReturn(ApiResponse.success("Job postings retrieved successfully", jobPostingResponseDtos))
                                        ));
                    })
                    .switchIfEmpty(Mono.just(ApiResponse.error("Candidate not found.")))
                    .onErrorResume(error -> {

                        log.error("Error fetching candidate by email: {}", email, error);

                        return Mono.just(ApiResponse.error("An error occurred while fetching job postings."));
                    });
        });
    }

    private Mono<String> fetchJobPage(String jobUrl) {

        long startTime = System.currentTimeMillis();

        log.info("[I/O - WEB] Starting HTTP request to fetch job page: {}", jobUrl);

        return webClient.get()
                .uri(jobUrl)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(html -> {

                    long duration = System.currentTimeMillis() - startTime;

                    log.info("[I/O - WEB] HTTP request completed in {}ms for URL: {} (response size: {} chars)", duration, jobUrl, html != null ? html.length() : 0);

                })
                .doOnError(e -> {

                    long duration = System.currentTimeMillis() - startTime;

                    log.error("[I/O - WEB] HTTP request failed after {}ms for URL: {}", duration, jobUrl, e);

                })
                .onErrorMap(JobPageFetchException::new);
    }

    private String extractReadableText(String html) {

        long startTime = System.currentTimeMillis();

        String result = Jsoup.parse(html).body().text();

        long duration = System.currentTimeMillis() - startTime;

        log.info("[CPU] HTML parsing completed in {}ms (output size: {} chars)", duration, result.length());

        return result;

    }

    private Mono<JobPostingRequestDto> callAiServiceForJobDetailsAsync(String jobText) {

        long startTime = System.currentTimeMillis();

        log.info("[I/O - AI] Starting AI service call for job extraction (text size: {} chars)", jobText.length());

        return Mono.fromCallable(() -> {

                    long aiCallStart = System.currentTimeMillis();

                    JobPostingRequestDto result = chatClient.prompt()
                            .system(system -> system.text(jobPostingSystemMessage))
                            .user(user -> user.text(jobPostingUserMessage).param("JsoupJobUrlBodyContent", jobText))
                            .call()
                            .entity(JobPostingRequestDto.class);

                    long aiCallDuration = System.currentTimeMillis() - aiCallStart;

                    log.info("[I/O - AI] AI extraction completed in {}ms", aiCallDuration);

                    return result;

                }).doOnSuccess(dto -> {

                    long duration = System.currentTimeMillis() - startTime;

                    log.info("[I/O - AI] Total AI service call completed in {}ms", duration);

                })
                .doOnError(e -> {

                    long duration = System.currentTimeMillis() - startTime;

                    log.error("[I/O - AI] AI service call failed after {}ms", duration, e);

                })
                .onErrorMap(AiExtractionException::new);
    }

    private JobPosting buildJobPosting(String candidateId, String jobUrl, JobPostingRequestDto dto) {

        return JobPosting.builder()
                .id(UUID.randomUUID().toString())
                .candidateId(candidateId)
                .jobUrl(jobUrl)
                .title(dto.getJobTitle())
                .company(dto.getCompanyName())
                .location(dto.getJobLocation())
                .description(dto.getJobDescription())
                .deadline(dto.getApplicationDeadline())
                .build();
    }

    private Mono<Void> invalidateJobPostingsCache(String candidateId) {

        return Mono.defer(() -> {

            String cacheKey = JOB_POSTING_CACHE_KEY + candidateId;

            return reactiveRedisOperations.delete(cacheKey)
                    .doOnSuccess(deleted -> log.info("Invalidated job postings cache for candidateId: {}", candidateId))
                    .doOnError(error -> log.error("Error invalidating job postings cache for candidateId: {}", candidateId, error))
                    .then();
        });
    }
}