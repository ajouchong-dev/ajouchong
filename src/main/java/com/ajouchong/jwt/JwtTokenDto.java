package com.ajouchong.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenDto {
    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
