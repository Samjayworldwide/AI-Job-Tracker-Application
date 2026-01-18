package com.samjay.AI_Powered.Job.Application.Tracker.configurations;

import com.samjay.AI_Powered.Job.Application.Tracker.security.JwtAuthenticationManager;
import com.samjay.AI_Powered.Job.Application.Tracker.security.JwtSecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationManager jwtAuthenticationManager;

    private final JwtSecurityContextRepository jwtSecurityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {

        return serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(jwtSecurityContextRepository)
                .authenticationManager(jwtAuthenticationManager)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(
                                "/api/candidates/signup",
                                "/api/candidates/login",
                                "/send-verification-code",
                                "/verify-code",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-doc/**",
                                "/webjars/**")
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, authEx) -> {

                            exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);

                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

                            return exchange.getResponse().setComplete();

                        })
                        .accessDeniedHandler((exchange, denied) -> {

                            exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);

                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);

                            return exchange.getResponse().setComplete();

                        })
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }
}