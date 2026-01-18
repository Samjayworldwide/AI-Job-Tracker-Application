package com.samjay.AI_Powered.Job.Application.Tracker.consumers;

import com.samjay.AI_Powered.Job.Application.Tracker.constants.ResumeProcessingStatus;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.VectorStorageRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.VECTOR_STORE_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaDLQConsumerService {

    private final ResumeRepository resumeRepository;

    @KafkaListener(topics = VECTOR_STORE_TOPIC + ".DLT")
    public void listenDLQ(VectorStorageRequestDto dto) {

        log.error("Moved to DLQ: resumeId={}", dto.resumeId());

        Integer result = resumeRepository.updateStatus(dto.resumeId(), ResumeProcessingStatus.FAILED).block();

        if (result == null || result == 0) {

            log.error("Failed to update processing status to FAILED for resumeId={}", dto.resumeId());

        } else {

            log.info("Updated processing status to FAILED for resumeId={}", dto.resumeId());

        }
    }
}
