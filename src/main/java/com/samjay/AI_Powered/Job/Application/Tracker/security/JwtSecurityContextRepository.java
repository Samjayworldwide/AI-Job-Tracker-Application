package com.samjay.AI_Powered.Job.Application.Tracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtAuthenticationManager jwtAuthenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {

        return Mono.empty();

    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/api/candidates/signup") ||
                path.startsWith("/api/candidates/login") ||
                path.startsWith("/send-verification-code") ||
                path.startsWith("/verify-code") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars")) {

            return Mono.empty();

        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            return Mono.empty();

        }

        String authToken = authHeader.substring(7);

        Authentication authentication = new UsernamePasswordAuthenticationToken(authToken, authToken);

        return jwtAuthenticationManager.authenticate(authentication).map(SecurityContextImpl::new);

    }
}