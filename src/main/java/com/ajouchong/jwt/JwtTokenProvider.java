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
    private static final long accessTokenValidity = 1000L * 60 * 60 * 24; // 1일

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32) { // 최소 길이 검증
            throw new IllegalArgumentException("JWT Secret key는 최소 32 bytes이어야 합니다.");
        }
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public static String createAccessToken(Member member) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .setSubject("accessToken")
                .setClaims(createAccessTokenClaims(member))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private static Map<String, Object> createAccessTokenClaims (Member member) {
        Map<String, Object> map = new HashMap<>();
        map.put("email", member.getEmail());
        map.put("name", member.getName());
        map.put("role", member.getRole());
        return map;
    }

    public String createRefreshToken(Member member) {
        Date now = new Date();

        long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7;
        Date expiryDate = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
                .setSubject("refreshToken")
                .setClaims(createRefreshTokenClaims(member))
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private static Map<String, Object> createRefreshTokenClaims (Member member) {
        Map<String, Object> map = new HashMap<>();
        map.put("email", member.getEmail());
        return map;
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // 만료된 경우 true 반환
        } catch (JwtException e) {
            return true; // 유효하지 않은 토큰도 만료된 것으로 처리
        }
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    public String getNameFromToken(String token) {
        return getClaimsFromToken(token).get("name", String.class);
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException("토큰이 만료되었습니다.");
        } catch (JwtException e) {
            throw new InvalidJwtException("유효하지 않은 JWT 토큰입니다.");
        }
    }

    public void setJwtCookie(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true); // HTTPS 환경에서만 전송
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 120); // 120분 후 만료
        response.addCookie(accessCookie);

        if (refreshToken != null) {
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일 후 만료
            response.addCookie(refreshCookie);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰이 만료되었습니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT 서명 검증 실패: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static class InvalidJwtException extends RuntimeException {
        public InvalidJwtException(String message) {
            super(message);
        }
    }
}
