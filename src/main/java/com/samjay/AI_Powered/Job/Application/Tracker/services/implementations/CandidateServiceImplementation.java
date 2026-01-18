package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.CandidateRegistrationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.LoginRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.LoginResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.Candidate;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.CandidateRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.security.JwtUtil;
import com.samjay.AI_Powered.Job.Application.Tracker.services.CandidateService;
import com.samjay.AI_Powered.Job.Application.Tracker.services.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateServiceImplementation implements CandidateService {

    private final CandidateRepository candidateRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailVerificationService emailVerificationService;

    private final JwtUtil jwtUtil;

    @Override
    public Mono<ApiResponse<String>> registerCandidate(Mono<CandidateRegistrationRequestDto> candidateRegistrationRequestDtoMono) {

        return candidateRegistrationRequestDtoMono.flatMap(candidateRegistrationRequestDto ->
                candidateRepository.existsByEmail(candidateRegistrationRequestDto.getEmail())
                        .flatMap(emailExists -> {

                            if (emailExists)
                                return Mono.just(ApiResponse.<String>error("Email is already in use"));

                            return emailVerificationService.isEmailVerified(candidateRegistrationRequestDto.getEmail())
                                    .flatMap(isVerified -> {

                                        if (!isVerified)
                                            return Mono.just(ApiResponse.<String>error("Email is not verified. Please verify your email before registering"));

                                        if (!candidateRegistrationRequestDto.getPassword().trim().equals(candidateRegistrationRequestDto.getConfirmPassword().trim()))
                                            return Mono.just(ApiResponse.<String>error("Passwords do not match"));

                                        Candidate candidate = Candidate
                                                .builder()
                                                .id(UUID.randomUUID().toString())
                                                .firstName(candidateRegistrationRequestDto.getFirstName())
                                                .lastName(candidateRegistrationRequestDto.getLastName())
                                                .email(candidateRegistrationRequestDto.getEmail().trim())
                                                .password(passwordEncoder.encode(candidateRegistrationRequestDto.getPassword().trim()))
                                                .build();

                                        return candidateRepository.save(candidate)
                                                .thenReturn(ApiResponse.<String>success("Registration successful"));
                                    });
                        })
                        .onErrorResume(error -> {

                            log.error("Error registering candidate with email {}", error.getMessage());

                            return Mono.just(ApiResponse.error("An unexpected error occurred completing your registration"));
                        })
        );
    }

    @Override
    public Mono<ApiResponse<LoginResponse>> loginCandidate(Mono<LoginRequestDto> loginRequestDtoMono) {

        return loginRequestDtoMono.flatMap(loginRequestDto -> candidateRepository.findByEmail(loginRequestDto.getEmail())
                .flatMap(candidate -> {

                    if (!passwordEncoder.matches(loginRequestDto.getPassword(), candidate.getPassword()))
                        return Mono.just(ApiResponse.<LoginResponse>error("Invalid email or password"));

                    return Mono.fromCallable(() -> jwtUtil.generateToken(candidate.getEmail(), null))
                            .map(jwtToken -> {

                                LoginResponse loginResponse = LoginResponse
                                        .builder()
                                        .token(jwtToken)
                                        .firstname(candidate.getFirstName())
                                        .lastname(candidate.getLastName())
                                        .build();

                                return ApiResponse.success("Login successful", loginResponse);

                            });
                })
                .switchIfEmpty(Mono.just(ApiResponse.error("Invalid email or password")))
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred logging in candidate: {}", error.getMessage());

                    return Mono.just(ApiResponse.error("An unexpected error occurred during login"));
                })
        );
    }
}