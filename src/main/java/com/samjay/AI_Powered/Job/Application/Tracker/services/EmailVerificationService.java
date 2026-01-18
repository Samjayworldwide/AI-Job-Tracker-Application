package com.samjay.AI_Powered.Job.Application.Tracker.services;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailVerificationCodeRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import reactor.core.publisher.Mono;

public interface EmailVerificationService {

    Mono<ApiResponse<String>> sendVerificationCode(String email);

    Mono<ApiResponse<String>> verifyCode(Mono<EmailVerificationCodeRequestDto> emailVerificationCodeRequestDtoMono);

    Mono<Boolean> isEmailVerified(String email);

}