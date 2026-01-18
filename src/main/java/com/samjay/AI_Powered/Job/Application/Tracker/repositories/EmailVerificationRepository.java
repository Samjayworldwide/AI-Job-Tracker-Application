package com.samjay.AI_Powered.Job.Application.Tracker.repositories;

import com.samjay.AI_Powered.Job.Application.Tracker.entities.EmailVerification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface EmailVerificationRepository extends R2dbcRepository<EmailVerification, String> {

    Mono<EmailVerification> findByEmail(String email);

}