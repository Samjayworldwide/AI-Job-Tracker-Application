package com.samjay.AI_Powered.Job.Application.Tracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        String token = authentication.getCredentials() == null ? null : authentication.getCredentials().toString();

        if (token == null || token.isEmpty()) {

            return Mono.empty();

        }

        if (!jwtUtil.validateToken(token)) {

            return Mono.empty();

        }

        String username = jwtUtil.extractUsername(token);

        var roles = jwtUtil.getRolesFromToken(token);

        var authorities = roles
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);

        return Mono.just(usernamePasswordAuthenticationToken);

    }
}