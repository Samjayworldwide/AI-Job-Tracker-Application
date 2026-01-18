package com.samjay.AI_Powered.Job.Application.Tracker.handlers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.JobFitEvaluationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApplicationResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.services.ApplicationService;
import com.samjay.AI_Powered.Job.Application.Tracker.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ApplicationHandler {

    private final ApplicationService applicationService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> streamAiJobRecommendationHandler(ServerRequest serverRequest) {

        Mono<JobFitEvaluationRequestDto> jobFitEvaluationRequestDtoMono = serverRequest.bodyToMono(JobFitEvaluationRequestDto.class)
                .doOnNext(requestValidator::validate);

        Flux<String> recommendationsFlux = applicationService.getAiRecommendationForJob(jobFitEvaluationRequestDtoMono);

        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(recommendationsFlux, String.class);

    }

    public Mono<ServerResponse> getAllApplicationsWithAiSuggestionsHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<ApplicationResponseDto>>> apiResponseMono = applicationService.getAllApplicationsWithAiSuggestions();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }
}