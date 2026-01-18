package com.samjay.AI_Powered.Job.Application.Tracker.services;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.CandidateRegistrationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.LoginRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.LoginResponse;
import reactor.core.publisher.Mono;

public interface CandidateService {

    Mono<ApiResponse<String>> registerCandidate(Mono<CandidateRegistrationRequestDto> candidateRegistrationRequestDtoMono);

    Mono<ApiResponse<LoginResponse>> loginCandidate(Mono<LoginRequestDto> loginRequestDtoMono);

}