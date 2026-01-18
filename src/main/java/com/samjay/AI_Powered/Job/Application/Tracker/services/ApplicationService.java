package com.samjay.AI_Powered.Job.Application.Tracker.services;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.JobFitEvaluationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApplicationResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationService {

    Flux<String> getAiRecommendationForJob(Mono<JobFitEvaluationRequestDto> jobFitEvaluationRequestDtoMono);

    Mono<ApiResponse<List<ApplicationResponseDto>>> getAllApplicationsWithAiSuggestions();

}