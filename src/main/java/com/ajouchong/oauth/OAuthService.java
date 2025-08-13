package com.ajouchong.oauth;

public interface OAuthService {
    GoogleUserDto getUserInfo(String accessToken);
} 