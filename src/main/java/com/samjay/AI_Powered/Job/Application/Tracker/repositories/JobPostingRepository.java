package com.samjay.AI_Powered.Job.Application.Tracker.repositories;

import com.samjay.AI_Powered.Job.Application.Tracker.entities.JobPosting;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface JobPostingRepository extends R2dbcRepository<JobPosting, String> {

    Flux<JobPosting> findAllByCandidateId(String candidateId);

}