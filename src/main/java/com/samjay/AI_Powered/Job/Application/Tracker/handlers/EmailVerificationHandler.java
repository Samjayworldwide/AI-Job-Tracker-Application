package com.samjay.AI_Powered.Job.Application.Tracker.handlers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailVerificationCodeRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.globalexception.RequestValidationException;
import com.samjay.AI_Powered.Job.Application.Tracker.services.EmailVerificationService;
import com.samjay.AI_Powered.Job.Application.Tracker.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationHandler {

    private final EmailVerificationService emailVerificationService;

    private final RequestValidator requestValidator;

    public Mono<ServerResponse> sendVerificationCodeHandler(ServerRequest serverRequest) {

        Optional<String> emailOptional = serverRequest.queryParam("email");

        if (emailOptional.isEmpty())
            throw new RequestValidationException("Email is required");

        Mono<ApiResponse<String>> apiResponseMono = emailVerificationService.sendVerificationCode(emailOptional.get());

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }

    public Mono<ServerResponse> verifyCodeHandler(ServerRequest serverRequest) {

        Mono<EmailVerificationCodeRequestDto> emailVerificationCodeRequestDtoMono = serverRequest.bodyToMono(EmailVerificationCodeRequestDto.class)
                .doOnNext(requestValidator::validate);

        Mono<ApiResponse<String>> apiResponseMono = emailVerificationService.verifyCode(emailVerificationCodeRequestDtoMono);

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }
}