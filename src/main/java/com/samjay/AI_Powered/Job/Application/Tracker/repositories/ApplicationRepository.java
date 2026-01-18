package com.samjay.AI_Powered.Job.Application.Tracker.repositories;

import com.samjay.AI_Powered.Job.Application.Tracker.entities.Application;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ApplicationRepository extends R2dbcRepository<Application, String> {

    Flux<Application> findAllByCandidateId(String candidateId);

}