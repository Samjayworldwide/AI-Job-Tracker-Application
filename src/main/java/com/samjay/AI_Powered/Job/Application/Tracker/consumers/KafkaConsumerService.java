package com.samjay.AI_Powered.Job.Application.Tracker.consumers;

import com.samjay.AI_Powered.Job.Application.Tracker.constants.ResumeProcessingStatus;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.VectorStorageRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.Resume;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ResumeRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.DataLoaderAndStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.CACHE_KEY_CANDIDATE_RESUMES;
import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.VECTOR_STORE_TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final DataLoaderAndStorageService dataLoaderAndStorageService;

    private final ResumeRepository resumeRepository;

    private final ReactiveRedisOperations<String, Object> reactiveRedisOperations;


    @KafkaListener(topics = VECTOR_STORE_TOPIC, groupId = "ai-job-application-tracker-group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(VectorStorageRequestDto vectorStorageRequestDto) {

        log.info("Received vector storage event for resumeId={}", vectorStorageRequestDto.resumeId());

        Resume resume = resumeRepository.findById(vectorStorageRequestDto.resumeId())
                .block();

        if (resume == null) {

            log.error("Resume {} not found. Skipping vector storage processing.", vectorStorageRequestDto.resumeId());

            return;
        }

        if (resume.getProcessingStatus() == ResumeProcessingStatus.COMPLETED) {

            log.info("Resume {} already processed. Skipping.", vectorStorageRequestDto.resumeId());

            return;
        }

        Integer result = resumeRepository.updateStatus(vectorStorageRequestDto.resumeId(), ResumeProcessingStatus.PROCESSING).block();

        if (result == null || result == 0) {

            log.error("Failed to update processing status for resumeId={}. Skipping vector storage processing.", vectorStorageRequestDto.resumeId());

            return;
        }

        try {

            dataLoaderAndStorageService.loadAndStoreResumeToVectorStore(vectorStorageRequestDto.blobName(),
                            vectorStorageRequestDto.candidateId(),
                            vectorStorageRequestDto.resumeId())
                    .block();

            Integer completedResult = resumeRepository.updateStatus(vectorStorageRequestDto.resumeId(), ResumeProcessingStatus.COMPLETED).block();

            if (completedResult == null || completedResult == 0) {

                log.error("Failed to update processing status to COMPLETED for resumeId={}", vectorStorageRequestDto.resumeId());

            }

            reactiveRedisOperations.delete(CACHE_KEY_CANDIDATE_RESUMES +vectorStorageRequestDto.candidateId()).block();

            log.info("Completed vector storage processing for resumeId={}", vectorStorageRequestDto.resumeId());

        } catch (Exception ex) {

            log.error("Error processing vector storage for resumeId={}: {}", vectorStorageRequestDto.resumeId(), ex.getMessage());

            throw ex;

        }
    }
}