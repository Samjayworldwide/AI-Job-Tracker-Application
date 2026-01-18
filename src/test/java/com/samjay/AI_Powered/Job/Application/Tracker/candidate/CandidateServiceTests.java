package com.samjay.AI_Powered.Job.Application.Tracker.candidate;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.CandidateRegistrationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.LoginRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.LoginResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.Candidate;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.CandidateRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.security.JwtUtil;
import com.samjay.AI_Powered.Job.Application.Tracker.services.EmailVerificationService;
import com.samjay.AI_Powered.Job.Application.Tracker.services.implementations.CandidateServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CandidateServiceTests {

    private CandidateRepository candidateRepository;

    private PasswordEncoder passwordEncoder;

    private EmailVerificationService emailVerificationService;

    private JwtUtil jwtUtil;

    private CandidateServiceImplementation service;

    @BeforeEach
    void setup() {

        candidateRepository = mock(CandidateRepository.class);

        passwordEncoder = mock(PasswordEncoder.class);

        emailVerificationService = mock(EmailVerificationService.class);

        jwtUtil = mock(JwtUtil.class);

        service = new CandidateServiceImplementation(
                candidateRepository,
                passwordEncoder,
                emailVerificationService,
                jwtUtil
        );
    }

    @Test
    void registerCandidate_fails_whenEmailAlreadyExists() {

        CandidateRegistrationRequestDto dto = new CandidateRegistrationRequestDto();

        dto.setEmail("exists@example.com");

        dto.setPassword("pass");

        dto.setConfirmPassword("pass");

        dto.setFirstName("John");

        dto.setLastName("Doe");

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(service.registerCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("already in use");
                })
                .verifyComplete();

        verify(candidateRepository).existsByEmail(dto.getEmail());

        verify(emailVerificationService, never()).isEmailVerified(anyString());

        verify(candidateRepository, never()).save(any());

    }

    @Test
    void registerCandidate_fails_whenEmailNotVerified() {

        CandidateRegistrationRequestDto dto = new CandidateRegistrationRequestDto();

        dto.setEmail("notverified@example.com");

        dto.setPassword("pass");

        dto.setConfirmPassword("pass");

        dto.setFirstName("Jane");

        dto.setLastName("Doe");

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(Mono.just(false));

        when(emailVerificationService.isEmailVerified(dto.getEmail())).thenReturn(Mono.just(false));

        StepVerifier.create(service.registerCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("Email is not verified");
                })
                .verifyComplete();

        verify(candidateRepository).existsByEmail(dto.getEmail());

        verify(emailVerificationService).isEmailVerified(dto.getEmail());

        verify(candidateRepository, never()).save(any());

    }

    @Test
    void registerCandidate_fails_whenPasswordsDoNotMatch() {

        CandidateRegistrationRequestDto dto = new CandidateRegistrationRequestDto();

        dto.setEmail("user@example.com");

        dto.setPassword("pass1");

        dto.setConfirmPassword("pass2");

        dto.setFirstName("Jim");

        dto.setLastName("Beam");

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(Mono.just(false));

        when(emailVerificationService.isEmailVerified(dto.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(service.registerCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("Passwords do not match");
                })
                .verifyComplete();

        verify(candidateRepository).existsByEmail(dto.getEmail());

        verify(emailVerificationService).isEmailVerified(dto.getEmail());

        verify(candidateRepository, never()).save(any());

    }

    @Test
    void registerCandidate_succeeds_whenValidAndVerified() {

        CandidateRegistrationRequestDto dto = new CandidateRegistrationRequestDto();

        dto.setEmail("new@example.com");

        dto.setPassword("pass");

        dto.setConfirmPassword("pass");

        dto.setFirstName("Sam");

        dto.setLastName("Jay");

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(Mono.just(false));

        when(emailVerificationService.isEmailVerified(dto.getEmail())).thenReturn(Mono.just(true));

        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        when(candidateRepository.save(ArgumentMatchers.any(Candidate.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.registerCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isTrue();
                    assertThat(resp.getResponseMessage()).contains("Registration successful");
                })
                .verifyComplete();

        verify(candidateRepository).existsByEmail(dto.getEmail());

        verify(emailVerificationService).isEmailVerified(dto.getEmail());

        verify(passwordEncoder).encode("pass");

        verify(candidateRepository).save(any(Candidate.class));

    }

    @Test
    void registerCandidate_handlesUnexpectedError() {

        CandidateRegistrationRequestDto dto = new CandidateRegistrationRequestDto();

        dto.setEmail("err@example.com");

        dto.setPassword("pass");

        dto.setConfirmPassword("pass");

        dto.setFirstName("Err");

        dto.setLastName("Or");

        when(candidateRepository.existsByEmail(dto.getEmail())).thenReturn(Mono.error(new RuntimeException("DB down")));

        StepVerifier.create(service.registerCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("unexpected error");
                })
                .verifyComplete();

        verify(candidateRepository).existsByEmail(dto.getEmail());

    }

    @Test
    void loginCandidate_succeeds_withValidCredentials() {

        String email = "login@example.com";

        String raw = "password";

        String hashed = "hashed";

        Candidate candidate = Candidate.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .password(hashed)
                .firstName("First")
                .lastName("Last")
                .build();

        when(candidateRepository.findByEmail(email)).thenReturn(Mono.just(candidate));

        when(passwordEncoder.matches(raw, hashed)).thenReturn(true);

        when(jwtUtil.generateToken(email, null)).thenReturn("jwt-token");

        LoginRequestDto dto = new LoginRequestDto();

        dto.setEmail(email);

        dto.setPassword(raw);

        StepVerifier.create(service.loginCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isTrue();
                    assertThat(resp.getResponseMessage()).contains("Login successful");
                    LoginResponse body = resp.getResponseBody();
                    assertThat(body.getToken()).isEqualTo("jwt-token");
                    assertThat(body.getFirstname()).isEqualTo("First");
                    assertThat(body.getLastname()).isEqualTo("Last");
                })
                .verifyComplete();

        verify(candidateRepository).findByEmail(email);

        verify(passwordEncoder).matches(raw, hashed);

        verify(jwtUtil).generateToken(email, null);

    }

    @Test
    void loginCandidate_fails_withInvalidPassword() {

        String email = "login@example.com";

        String raw = "bad";

        String hashed = "hashed";

        Candidate candidate = Candidate.builder()
                .id(UUID.randomUUID().toString())
                .email(email)
                .password(hashed)
                .firstName("First")
                .lastName("Last")
                .build();

        when(candidateRepository.findByEmail(email)).thenReturn(Mono.just(candidate));

        when(passwordEncoder.matches(raw, hashed)).thenReturn(false);

        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(raw);

        StepVerifier.create(service.loginCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("Invalid email or password");
                })
                .verifyComplete();

        verify(candidateRepository).findByEmail(email);

        verify(passwordEncoder).matches(raw, hashed);

        verify(jwtUtil, never()).generateToken(anyString(), any());

    }

    @Test
    void loginCandidate_fails_whenUserNotFound() {

        String email = "missing@example.com";

        when(candidateRepository.findByEmail(email)).thenReturn(Mono.empty());

        LoginRequestDto dto = new LoginRequestDto();

        dto.setEmail(email);

        dto.setPassword("any");

        StepVerifier.create(service.loginCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("Invalid email or password");
                })
                .verifyComplete();

        verify(candidateRepository).findByEmail(email);

        verify(passwordEncoder, never()).matches(anyString(), anyString());

        verify(jwtUtil, never()).generateToken(anyString(), any());

    }

    @Test
    void loginCandidate_handlesUnexpectedError() {

        String email = "err@example.com";

        when(candidateRepository.findByEmail(email)).thenReturn(Mono.error(new RuntimeException("boom")));

        LoginRequestDto dto = new LoginRequestDto();

        dto.setEmail(email);

        dto.setPassword("pass");

        StepVerifier.create(service.loginCandidate(Mono.just(dto)))
                .assertNext(resp -> {
                    assertThat(resp.isSuccessful()).isFalse();
                    assertThat(resp.getResponseMessage()).contains("unexpected error");
                })
                .verifyComplete();

        verify(candidateRepository).findByEmail(email);

    }
}