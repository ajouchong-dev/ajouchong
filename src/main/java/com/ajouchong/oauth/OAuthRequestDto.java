package com.ajouchong.oauth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthRequestDto {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
}
