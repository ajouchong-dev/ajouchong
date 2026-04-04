package com.ajouchong.jwt;

import com.ajouchong.entity.Member;
import com.ajouchong.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/login/auth/oauth",
            "/api/login/auth/logout"
    );

    
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return PUBLIC_AUTH_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtTokenProvider.isExpired(token)) {
                log.warn("만료된 토큰으로 인증 시도: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                sendErrorResponse(response, "Token has expired.");
                return;
            }

            setAuthentication(token);
            log.debug("JWT 인증 성공: {}", request.getRequestURI());
        } catch (Exception e) {
            log.warn("JWT 인증 실패: {} - {}", request.getRequestURI(), e.getMessage());
            sendErrorResponse(response, "Invalid token: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader(JwtConstants.AUTHORIZATION_HEADER);
        if (authorization != null && authorization.startsWith(JwtConstants.BEARER_PREFIX)) {
            return authorization.substring(JwtConstants.BEARER_PREFIX.length());
        }

        return extractTokenFromCookies(request);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (JwtConstants.ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                try {
                    return java.net.URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("쿠키 디코딩 실패: {}", e.getMessage());
                    return null;
                }
            }
        }
        return null;
    }

    private void setAuthentication(String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);
        String authorityRole = role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role;

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("토큰의 이메일로 회원을 찾을 수 없음: {}", email);
                    return new IllegalArgumentException("User not found: " + email);
                });

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(authorityRole))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(JwtConstants.ERROR_RESPONSE_TEMPLATE, message));
    }
}
