package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailVerificationCodeRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.EmailVerification;
import com.samjay.AI_Powered.Job.Application.Tracker.publishers.EmailPublisher;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.EmailVerificationRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.EmailVerificationService;
import com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.getVerificationCodeMailBody;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImplementation implements EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;

    private final EmailPublisher emailPublisher;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<ApiResponse<String>> sendVerificationCode(String email) {

        /*
        I used Mono.defer here to prevent the code generation and code hashing to be executed at the time
        when the sendVerificationCode method is called. This ensures that a new code is generated
        and hashed each time the Mono is subscribed to, which is important for the email verification
        process to work correctly.
         */

        return Mono.defer(() -> {

            String code = Utility.generateVerificationCode();

            String hashedCode = passwordEncoder.encode(code);

            return emailVerificationRepository.findByEmail(email)
                    .flatMap(existing -> {

                        existing.setVerificationCode(hashedCode);

                        existing.setVerified(false);

                        return emailVerificationRepository.save(existing);
                    })
                    .switchIfEmpty(emailVerificationRepository.save(EmailVerification
                                    .builder()
                                    .id(UUID.randomUUID().toString())
                                    .email(email)
                                    .verificationCode(hashedCode)
                                    .isVerified(false)
                                    .build()
                            )
                    )
                    .flatMap(saved -> {

                                String emailBody = getVerificationCodeMailBody(code);

                                EmailRequestDto emailRequestDto = new EmailRequestDto(saved.getEmail(), emailBody, "Email Verification Code");

                                return emailPublisher.queueEmail(emailRequestDto).thenReturn(ApiResponse.success("A verification code has been sent to your email address"));
                            }
                    );
        });
    }

    @Override
    public Mono<ApiResponse<String>> verifyCode(Mono<EmailVerificationCodeRequestDto> emailVerificationCodeRequestDtoMono) {

        return emailVerificationCodeRequestDtoMono.flatMap(emailVerificationCodeRequestDto -> emailVerificationRepository
                .findByEmail(emailVerificationCodeRequestDto.getEmail())
                .flatMap(emailVerification -> {

                    if (emailVerification.getDateCreated().plusMinutes(10L).isBefore(Utility.getCurrentLocalDateTime()))
                        return Mono.just(ApiResponse.<String>error("The verification code has expired. Please request a new code."));

                    if (!passwordEncoder.matches(emailVerificationCodeRequestDto.getCode(), emailVerification.getVerificationCode()))
                        return Mono.just(ApiResponse.<String>error("The verification code is incorrect. Please try again."));

                    emailVerification.setVerified(true);

                    return emailVerificationRepository.save(emailVerification)
                            .thenReturn(ApiResponse.<String>success("Email verified successfully"));
                })
                .switchIfEmpty(Mono.just(ApiResponse.error("No verification code found for this email")))
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred verifying email code for {}: {}", emailVerificationCodeRequestDto.getEmail(), error.getMessage());

                    return Mono.just(ApiResponse.error("An unexpected error occurred verifying the code"));
                })
        );
    }

    @Override
    public Mono<Boolean> isEmailVerified(String email) {

        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .defaultIfEmpty(false)
                .onErrorResume(error -> {

                    log.error("An unexpected error occurred checking if email {} is verified: {}", email, error.getMessage());

                    return Mono.just(false);
                });
    }
}