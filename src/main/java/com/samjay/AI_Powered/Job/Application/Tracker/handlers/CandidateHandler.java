package com.samjay.AI_Powered.Job.Application.Tracker.handlers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.CandidateRegistrationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.LoginRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.LoginResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.services.CandidateService;
import com.samjay.AI_Powered.Job.Application.Tracker.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CandidateHandler {

    private final CandidateService candidateService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> signUpCandidateHandler(ServerRequest serverRequest) {

        Mono<CandidateRegistrationRequestDto> candidateRegistrationRequestDtoMono = serverRequest.bodyToMono(CandidateRegistrationRequestDto.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = candidateService.registerCandidate(candidateRegistrationRequestDtoMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }

    public Mono<ServerResponse> loginCandidateHandler(ServerRequest serverRequest) {

        Mono<LoginRequestDto> loginRequestDtoMono = serverRequest.bodyToMono(LoginRequestDto.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<LoginResponse>> apiResponseMono = candidateService.loginCandidate(loginRequestDtoMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }
}