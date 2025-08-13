package com.ajouchong.jwt;

public final class JwtConstants {
    
    // 토큰 타입
    public static final String ACCESS_TOKEN_SUBJECT = "accessToken";
    public static final String REFRESH_TOKEN_SUBJECT = "refreshToken";
    
    // 클레임 키
    public static final String EMAIL_CLAIM = "email";
    public static final String NAME_CLAIM = "name";
    public static final String ROLE_CLAIM = "role";
    
    // 쿠키 이름
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    
    // 토큰 유효기간 (밀리초)
    public static final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60 * 24; // 1일
    public static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일
    
    // 쿠키 유효기간 (초)
    public static final int ACCESS_TOKEN_COOKIE_MAX_AGE = 60 * 120; // 120분
    public static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // 7일
    
    // 보안 설정
    public static final int MIN_SECRET_KEY_LENGTH = 32;
    
    // HTTP 헤더
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // 응답 메시지
    public static final String ERROR_RESPONSE_TEMPLATE = "{\"error\": \"Unauthorized\", \"message\": \"%s\"}";
    
    private JwtConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
} 