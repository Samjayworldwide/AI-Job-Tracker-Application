package com.samjay.AI_Powered.Job.Application.Tracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private SecretKey key;

    @Value("${jwt.key}")
    private String jwtKey;

    @PostConstruct
    public void init() {

        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtKey));
    }

    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);

    }

    public Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);

    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);

    }

    public Boolean validateToken(String token) {

        try {

            return !isTokenExpired(token);

        } catch (Exception e) {

            return false;

        }
    }

    public String generateToken(String username, List<String> roles) {

        return createToken(username, roles);

    }

    public List<String> getRolesFromToken(String token) {

        Claims claims = extractAllClaims(token);

        Object roles = claims.get("roles");

        if (roles instanceof List) {

            return ((List<?>) roles).stream().map(Object::toString).collect(Collectors.toList());

        }

        return Collections.emptyList();
    }

    private Claims extractAllClaims(String token) {

        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());

    }

    private String createToken(String username, List<String> roles) {

        return Jwts.builder()
                .issuer("Ai-Job-Application-Tracker")
                .subject(username)
                .claim("email", username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }
}