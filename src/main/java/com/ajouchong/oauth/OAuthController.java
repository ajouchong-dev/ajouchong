package com.ajouchong.oauth;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.ProfileResponseDto;
import com.ajouchong.entity.Member;
import com.ajouchong.entity.enumClass.MemberRole;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/login/auth", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @PostMapping("/oauth")
    public ApiResponse<OAuthResponseDto> googleLogin(@RequestBody Map<String, String> requestBody, HttpServletResponse response) {
        log.info("[Google Login] 요청 수신: {}", requestBody);

        String accessToken = requestBody.get("accessToken");
        // String jwtToken = requestBody.get("jwtToken");
        String refreshToken = requestBody.get("refreshToken");

        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("AccessToken이 없습니다.");
            throw new IllegalArgumentException("AccessToken is missing.");
        }

        GoogleUserDto googleUser = googleOAuthService.getUserInfo(accessToken);

        Member member = memberRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> {
                    Member newMember = new Member(googleUser);
                    newMember.setRole(MemberRole.STUDENT); // 기본 역할 지정
                    return memberRepository.save(newMember);
                });

        String newJwtAccessToken;
        String newJwtRefreshToken = null;

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            log.info("유효한 리프레시 토큰 존재. 새로운 JWT 액세스 토큰 발급 중...");
            newJwtAccessToken = JwtTokenProvider.createAccessToken(member);
            jwtTokenProvider.setJwtCookie(response, newJwtAccessToken, refreshToken);
        } else {
            log.info("리프레시 토큰 없음 또는 유효하지 않음. 새로운 JWT 토큰 발급...");
            newJwtAccessToken = JwtTokenProvider.createAccessToken(member);
            newJwtRefreshToken = jwtTokenProvider.createRefreshToken(member);
            jwtTokenProvider.setJwtCookie(response, newJwtAccessToken, newJwtRefreshToken);
        }

        OAuthResponseDto responseDto = new OAuthResponseDto(newJwtAccessToken, member);
        log.info("신규 JWT 발급 완료: {}", newJwtAccessToken);
        log.info("res {}", responseDto);

        return new ApiResponse<>(1, "Google login 성공", responseDto);
    }

    @GetMapping("/info")
    public ApiResponse<ProfileResponseDto> getUserInfo(@CookieValue(value = "accessToken", required = false) String accessToken,
            HttpServletRequest request, HttpServletResponse response) {

        log.info("[회원 정보 조회] 요청 수신. accessToken 쿠키 값: {}", accessToken);

        if (accessToken == null || accessToken.isEmpty()) {
            log.warn("accessToken 쿠키가 없음.");
            return new ApiResponse<>(0, "accessToken이 유효하지 않거나 없습니다.", null);
        }

        String email;
        try {
            email = jwtTokenProvider.getEmailFromToken(accessToken);
        } catch (JwtTokenProvider.InvalidJwtException e) {
            log.error("JWT 파싱 실패: {}", e.getMessage());
            return new ApiResponse<>(0, "Invalid token", null);
        }

        if (jwtTokenProvider.isExpired(accessToken)) {
            log.warn("JWT 만료됨. 리프레시 토큰 검토 중...");
            String refreshToken = jwtTokenProvider.getRefreshTokenFromCookie(request);

            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                log.info("유효한 리프레시 토큰 확인. 새로운 JWT 액세스 토큰 발급 중...");
                Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> {
                            log.error("회원 정보 없음: {}", email);
                            return new RuntimeException("회원 정보가 없습니다.");
                        });

                accessToken = JwtTokenProvider.createAccessToken(member);
                jwtTokenProvider.setJwtCookie(response, accessToken, refreshToken); // 쿠키에 새로운 accessToken 저장
            } else {
                log.error("리프레시 토큰이 없거나 유효하지 않음.");
                return new ApiResponse<>(0, "세션이 만료되었습니다. 다시 로그인하세요.", null);
            }
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("회원 정보 없음: {}", email);
                    return new RuntimeException("회원 정보가 없습니다.");
                });

        ProfileResponseDto responseDto = ProfileResponseDto.builder()
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();

        log.info("성공");
        return new ApiResponse<>(1, "회원 정보 조회 성공", responseDto);
    }

    @PostMapping("logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        Cookie refreshToken = new Cookie("refreshToken", null);
        refreshToken.setHttpOnly(true);
        refreshToken.setSecure(true);
        refreshToken.setPath("/");
        refreshToken.setMaxAge(0); // 쿠키 즉시 만료

        Cookie accessToken = new Cookie("accessToken", null);
        accessToken.setHttpOnly(true);
        accessToken.setSecure(true);
        accessToken.setPath("/");
        accessToken.setMaxAge(0); // 쿠키 즉시 만료

        response.addCookie(refreshToken);
        response.addCookie(accessToken);

        return new ApiResponse<>(1, "로그아웃 되었습니다.", null);
    }

}
