package com.ajouchong.jwt;

import com.ajouchong.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private static SecretKey secretKey = null;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        validateAndInitializeSecretKey(secret);
    }

    private void validateAndInitializeSecretKey(String secret) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            if (keyBytes.length < JwtConstants.MIN_SECRET_KEY_LENGTH) {
                throw new IllegalArgumentException("JWT Secret key는 최소 " + JwtConstants.MIN_SECRET_KEY_LENGTH + " bytes이어야 합니다.");
            }
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.error("JWT Secret key 초기화 실패: {}", e.getMessage());
            throw e;
        }
    }

    public static String createAccessToken(Member member) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JwtConstants.ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(JwtConstants.ACCESS_TOKEN_SUBJECT)
                .setClaims(createAccessTokenClaims(member))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private static Map<String, Object> createAccessTokenClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.EMAIL_CLAIM, member.getEmail());
        claims.put(JwtConstants.NAME_CLAIM, member.getName());
        claims.put(JwtConstants.ROLE_CLAIM, member.getRole());
        return claims;
    }

    public String createRefreshToken(Member member) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JwtConstants.REFRESH_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(JwtConstants.REFRESH_TOKEN_SUBJECT)
                .setClaims(createRefreshTokenClaims(member))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private static Map<String, Object> createRefreshTokenClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.EMAIL_CLAIM, member.getEmail());
        return claims;
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            return true;
        } catch (JwtException e) {
            log.debug("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            return true;
        }
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get(JwtConstants.EMAIL_CLAIM, String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get(JwtConstants.ROLE_CLAIM, String.class);
    }

    public String getNameFromToken(String token) {
        return getClaimsFromToken(token).get(JwtConstants.NAME_CLAIM, String.class);
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            throw new InvalidJwtException("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            throw new InvalidJwtException("유효하지 않은 JWT 토큰입니다.");
        }
    }

    public void setJwtCookie(HttpServletResponse response, String accessToken, String refreshToken) {
        setCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME, accessToken, JwtConstants.ACCESS_TOKEN_COOKIE_MAX_AGE);
        
        if (refreshToken != null) {
            setCookie(response, JwtConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, JwtConstants.REFRESH_TOKEN_COOKIE_MAX_AGE);
        }
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT 토큰이 만료되었습니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT 서명 검증 실패: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (JwtConstants.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearJwtCookies(HttpServletResponse response) {
        clearCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME);
        clearCookie(response, JwtConstants.REFRESH_TOKEN_COOKIE_NAME);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public static class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String message) {
            super(message);
        }
    }
}
