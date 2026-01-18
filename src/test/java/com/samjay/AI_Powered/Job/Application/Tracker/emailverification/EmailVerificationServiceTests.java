package com.samjay.AI_Powered.Job.Application.Tracker.emailverification;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailVerificationCodeRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.EmailVerification;
import com.samjay.AI_Powered.Job.Application.Tracker.publishers.EmailPublisher;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.EmailVerificationRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.implementations.EmailVerificationServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailVerificationServiceTests {

    private EmailVerificationRepository emailVerificationRepository;

    private PasswordEncoder passwordEncoder;

    private EmailPublisher emailPublisher;

    private EmailVerificationServiceImplementation service;

    @BeforeEach
    void setup() {

        emailVerificationRepository = mock(EmailVerificationRepository.class);

        emailPublisher = mock(EmailPublisher.class);

        passwordEncoder = mock(PasswordEncoder.class);

        service = new EmailVerificationServiceImplementation(
                emailVerificationRepository,
                emailPublisher,
                passwordEncoder
        );
    }

    @Test
    void sendVerificationCode_createsNewRecord_andQueuesEmail() {

        String email = "test@example.com";

        when(passwordEncoder.encode(anyString())).thenReturn("hashed-code");

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.empty());

        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> {

                    EmailVerification arg = invocation.getArgument(0);

                    return Mono.just(arg);

                });

        when(emailPublisher.queueEmail(any(EmailRequestDto.class))).thenReturn(Mono.empty());

        Mono<ApiResponse<String>> result = service.sendVerificationCode(email);

        StepVerifier.create(result)
                .assertNext(resp -> {

                    assertThat(resp.isSuccessful()).isTrue();

                    assertThat(resp.getResponseMessage()).contains("verification code");

                })
                .verifyComplete();

        verify(emailVerificationRepository).findByEmail(email);

        verify(emailVerificationRepository).save(any(EmailVerification.class));

        ArgumentCaptor<EmailRequestDto> captor = ArgumentCaptor.forClass(EmailRequestDto.class);

        verify(emailPublisher).queueEmail(captor.capture());

        assertThat(captor.getValue().recipient()).isEqualTo(email);

    }

    @Test
    void sendVerificationCode_updatesExistingRecord_andQueuesEmail() {

        EmailVerification ev = EmailVerification.builder()
                .id(UUID.randomUUID().toString())
                .email("ok@x.com")
                .verificationCode("old-hash")
                .isVerified(false)
                .dateCreated(LocalDateTime.now().minusMinutes(5))
                .build();

        when(emailVerificationRepository.findByEmail(any())).thenReturn(Mono.just(ev));

        when(passwordEncoder.encode(anyString())).thenReturn("new-hash");

        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        when(emailPublisher.queueEmail(any(EmailRequestDto.class))).thenReturn(Mono.empty());

        Mono<ApiResponse<String>> result = service.sendVerificationCode(ev.getEmail());

        StepVerifier.create(result)
                .expectNextMatches(ApiResponse::isSuccessful)
                .verifyComplete();
    }

    @Test
    void verifyCode_succeeds_whenCodeMatches_andNotExpired() {

        String email = "ok@x.com";

        String hashed = "hashed";

        String inputCode = "123456";

        EmailVerification ev = EmailVerification.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .verificationCode(hashed)
                .isVerified(false)
                .dateCreated(LocalDateTime.now().minusMinutes(5))
                .build();

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.just(ev));

        when(passwordEncoder.matches(inputCode, hashed)).thenReturn(true);

        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        EmailVerificationCodeRequestDto dto = new EmailVerificationCodeRequestDto();

        dto.setEmail(email);

        dto.setCode(inputCode);

        StepVerifier.create(service.verifyCode(Mono.just(dto)))
                .assertNext(resp -> {

                    assertThat(resp.isSuccessful()).isTrue();

                    assertThat(resp.getResponseMessage()).contains("verified successfully");

                })
                .verifyComplete();

        verify(emailVerificationRepository).save(any(EmailVerification.class));

    }

    @Test
    void verifyCode_fails_whenExpired() {

        String email = "exp@x.com";

        EmailVerification ev = EmailVerification.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .verificationCode("hash")
                .isVerified(false)
                .dateCreated(LocalDateTime.now().minusMinutes(20))
                .build();

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.just(ev));

        EmailVerificationCodeRequestDto dto = new EmailVerificationCodeRequestDto();

        dto.setEmail(email);

        dto.setCode("any");

        StepVerifier.create(service.verifyCode(Mono.just(dto)))
                .assertNext(resp -> {

                    assertThat(resp.isSuccessful()).isFalse();

                    assertThat(resp.getResponseMessage()).contains("expired");

                })
                .verifyComplete();

        verify(emailVerificationRepository, never()).save(any());

    }

    @Test
    void verifyCode_fails_whenIncorrectCode() {

        String email = "wrong@x.com";

        String hashed = "hashed";

        EmailVerification ev = EmailVerification.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .verificationCode(hashed)
                .isVerified(false)
                .dateCreated(LocalDateTime.now().minusMinutes(5))
                .build();

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.just(ev));

        when(passwordEncoder.matches("badcode", hashed)).thenReturn(false);

        EmailVerificationCodeRequestDto dto = new EmailVerificationCodeRequestDto();

        dto.setEmail(email);

        dto.setCode("badcode");

        StepVerifier.create(service.verifyCode(Mono.just(dto)))
                .assertNext(resp -> {

                    assertThat(resp.isSuccessful()).isFalse();

                    assertThat(resp.getResponseMessage()).contains("incorrect");

                })
                .verifyComplete();

        verify(emailVerificationRepository, never()).save(any());

    }

    @Test
    void verifyCode_fails_whenNoRecordFound() {

        String email = "missing@x.com";

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.empty());

        EmailVerificationCodeRequestDto dto = new EmailVerificationCodeRequestDto();

        dto.setEmail(email);

        dto.setCode("code");

        StepVerifier.create(service.verifyCode(Mono.just(dto)))
                .assertNext(resp -> {

                    assertThat(resp.isSuccessful()).isFalse();

                    assertThat(resp.getResponseMessage()).contains("No verification code found");

                })
                .verifyComplete();
    }

    @Test
    void verifyCode_handlesUnexpectedError() {

        String email = "err@x.com";

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.error(new RuntimeException("DB down")));

        EmailVerificationCodeRequestDto dto = new EmailVerificationCodeRequestDto();

        dto.setEmail(email);

        dto.setCode("code");

        StepVerifier.create(service.verifyCode(Mono.just(dto)))
                .assertNext(resp -> {

                    assertThat(resp.isSuccessful()).isFalse();

                    assertThat(resp.getResponseMessage()).contains("unexpected error");

                })
                .verifyComplete();
    }

    @Test
    void isEmailVerified_returnsTrue_whenVerified() {

        String email = "v@x.com";

        EmailVerification ev = EmailVerification.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .verificationCode("hash")
                .isVerified(true)
                .build();

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.just(ev));

        StepVerifier.create(service.isEmailVerified(email))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isEmailVerified_returnsFalse_whenNotFound() {

        String email = "nf@x.com";

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.empty());

        StepVerifier.create(service.isEmailVerified(email))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isEmailVerified_returnsFalse_onError() {

        String email = "err@x.com";

        when(emailVerificationRepository.findByEmail(email)).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(service.isEmailVerified(email))
                .expectNext(false)
                .verifyComplete();
    }
}
