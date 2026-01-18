package com.samjay.AI_Powered.Job.Application.Tracker.repositories;

import com.samjay.AI_Powered.Job.Application.Tracker.constants.ResumeProcessingStatus;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.Resume;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ResumeRepository extends R2dbcRepository<Resume, String> {

    @Modifying
    @Query("""
                UPDATE Resumes
                SET processing_status = :status
                WHERE id = :resumeId
            """)
    Mono<Integer> updateStatus(String resumeId, ResumeProcessingStatus status);

    Flux<Resume> findAllByCandidateIdAndProcessingStatus(String candidateId, ResumeProcessingStatus status);

}