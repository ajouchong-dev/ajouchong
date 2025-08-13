package com.ajouchong.jwt;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtAuthenticationResponse {
    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public JwtAuthenticationResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
