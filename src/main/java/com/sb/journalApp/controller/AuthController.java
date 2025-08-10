package com.sb.journalApp.controller;

import com.sb.journalApp.dto.LoginRequest;
import com.sb.journalApp.dto.TokenResponse;
import com.sb.journalApp.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Operation(security = {})
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        var u = userRepo.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("journalApp")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))   // 1h
                .subject(u.getUsername())
                .claim("uid", u.getId())
                .claim("roles", List.of("USER"))
                .build();

        // NEW: include header with HS256
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresInSeconds(3600)
                .build();
    }
}
