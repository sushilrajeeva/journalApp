package com.sb.journalApp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse { private String tokenType; private String accessToken; private long expiresInSeconds; }
