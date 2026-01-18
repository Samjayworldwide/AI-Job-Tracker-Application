package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.samjay.AI_Powered.Job.Application.Tracker.constants.ResumeProcessingStatus;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.VectorStorageRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ResumeResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.Resume;
import com.samjay.AI_Powered.Job.Application.Tracker.producers.KafkaProducerService;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.CandidateRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ResumeRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.BlobStorageService;
import com.samjay.AI_Powered.Job.Application.Tracker.services.ResumeService;
import com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class ResumeServiceImplementation implements ResumeService {

    private final ResumeRepository resumeRepository;

    private final CandidateRepository candidateRepository;

    private final BlobStorageService blobStorageService;

    private final KafkaProducerService kafkaProducerService;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    @Override
    public Mono<ApiResponse<String>> uploadResume(String resumeName, FilePart resumeFile) {

        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {

            String email = securityContext.getAuthentication().getName();

            return candidateRepository.findByEmail(email)
                    .flatMap(candidate -> blobStorageService.uploadFileToAzureBlobStorage(resumeFile, "resume/")
                            .flatMap(blobName -> {

                                Resume resume = Resume
                                        .builder()
                                        .id(UUID.randomUUID().toString())
                                        .blobName(blobName)
                                        .candidateId(candidate.getId())
                                        .resumeName(resumeName)
                                        .processingStatus(ResumeProcessingStatus.UPLOADED)
                                        .uploadedDate(Utility.getCurrentLocalDateTime())
                                        .build();

                                return resumeRepository.save(resume)
                                        .flatMap(savedResume -> {

                                            VectorStorageRequestDto vectorStorageRequestDto = new VectorStorageRequestDto(savedResume.getBlobName(), candidate.getId(), savedResume.getId());

                                            return kafkaProducerService.sendMessageAsync(VECTOR_STORE_TOPIC, savedResume.getId(), vectorStorageRequestDto)
                                                    .thenReturn(ApiResponse.<String>success("Resume uploaded successfully"))
                                                    .onErrorResume(kafkaError -> {

                                                        log.error("Error while sending message to Kafka for vector storage", kafkaError);

                                                        return resumeRepository.updateStatus(savedResume.getId(), ResumeProcessingStatus.FAILED)
                                                                .thenReturn(ApiResponse.error("Resume uploaded but failed to queue for processing. Please try again."));
                                                    });
                                        });
                            })
                    )
                    .switchIfEmpty(Mono.just(ApiResponse.error("Candidate not found with given logged in mail")))
                    .onErrorResume(error -> {

                        log.error("Error while uploading resume", error);

                        return Mono.just(ApiResponse.error("An error occurred while uploading resume"));

                    });
        });
    }

    @Override
    public Mono<ApiResponse<List<ResumeResponseDto>>> fetchAllCandidateResumes() {

        return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {

            String email = securityContext.getAuthentication().getName();

            return candidateRepository.findByEmail(email)
                    .flatMap(candidate -> {

                        String cacheKey = CACHE_KEY_CANDIDATE_RESUMES + candidate.getId();

                        return reactiveRedisOperations.opsForValue().get(cacheKey)
                                .flatMap(obj -> {

                                    if (obj instanceof List<?> cachedResumes && !cachedResumes.isEmpty() && cachedResumes.getFirst() instanceof ResumeResponseDto) {

                                        List<ResumeResponseDto> resumes = (List<ResumeResponseDto>) cachedResumes;

                                        log.info("Fetched resumes from cache for candidate");

                                        return Mono.just(ApiResponse.success("Resume retrieved successfully", resumes));

                                    } else {

                                        log.info("No cached resumes found for candidate, fetching from database");

                                        return Mono.empty();
                                    }
                                })
                                .onErrorResume(error -> {

                                    log.info("An unexpected error occurred fetching from redis");

                                    return Mono.empty();

                                })
                                .switchIfEmpty(resumeRepository.findAllByCandidateIdAndProcessingStatus(candidate.getId(), ResumeProcessingStatus.COMPLETED)
                                        .map(resume -> ResumeResponseDto
                                                .builder()
                                                .id(resume.getId())
                                                .resumeName(resume.getResumeName())
                                                .build()
                                        )
                                        .collectList()
                                        .flatMap(resumeResponseDtos -> reactiveRedisOperations.opsForValue()
                                                .set(cacheKey, resumeResponseDtos, CACHE_TTL)
                                                .doOnError(error -> log.error("An unexpected error occurred saving to redis {}", error.getMessage()))
                                                .thenReturn(ApiResponse.success("Resume retrieved successfully", resumeResponseDtos))
                                                .onErrorReturn(ApiResponse.success("Resume retrieved successfully", resumeResponseDtos))
                                        )
                                );
                    })
                    .switchIfEmpty(Mono.just(ApiResponse.error("Candidate not found with given logged in mail")))
                    .onErrorResume(error -> {

                        log.error("Error while fetching candidate resumes", error);

                        return Mono.just(ApiResponse.error("An error occurred while fetching resumes"));
                    });
        });
    }
}