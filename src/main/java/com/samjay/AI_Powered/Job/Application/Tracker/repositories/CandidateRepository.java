package com.samjay.AI_Powered.Job.Application.Tracker.repositories;

import com.samjay.AI_Powered.Job.Application.Tracker.entities.Candidate;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CandidateRepository extends R2dbcRepository<Candidate, String> {

    Mono<Boolean> existsByEmail(String email);

    Mono<Candidate> findByEmail(String email);

}