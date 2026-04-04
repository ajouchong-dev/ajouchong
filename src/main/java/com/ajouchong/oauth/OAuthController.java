package com.ajouchong.oauth;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.ProfileResponseDto;
import com.ajouchong.entity.Member;
import com.ajouchong.entity.enumClass.MemberRole;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/login/auth", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {
    private static final Set<String> ADMIN_EMAIL_ALLOWLIST = Set.of("toadsam@ajou.ac.kr");

    private final OAuthService oAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @PostMapping("/oauth")
    public ApiResponse<OAuthResponseDto> googleLogin(@RequestBody Map<String, String> requestBody, 
                                                    HttpServletResponse response) {
        log.info("[Google Login] 요청 수신");
        
        try {
            String accessToken = validateAndExtractAccessToken(requestBody);
            String refreshToken = requestBody.get("refreshToken");

            GoogleUserDto googleUser = oAuthService.getUserInfo(accessToken);
            Member member = findOrCreateMember(googleUser);
            
            String newJwtAccessToken = createNewAccessToken(member);
            String newJwtRefreshToken = handleRefreshToken(refreshToken, member, response);
            
            jwtTokenProvider.setJwtCookie(response, newJwtAccessToken, newJwtRefreshToken);

            OAuthResponseDto responseDto = new OAuthResponseDto(newJwtAccessToken, member);
            log.info("Google 로그인 성공: {}", member.getEmail());
            
            return new ApiResponse<>(1, "Google login 성공", responseDto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Google 로그인 요청 검증 실패: {}", e.getMessage());
            return new ApiResponse<>(0, e.getMessage(), null);
        } catch (OAuthException e) {
            log.error("OAuth 처리 중 오류 발생: {}", e.getMessage());
            return new ApiResponse<>(0, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Google 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            return new ApiResponse<>(0, "로그인 처리 중 오류가 발생했습니다.", null);
        }
    }

    @GetMapping("/info")
    public ApiResponse<ProfileResponseDto> getUserInfo(@CookieValue(value = "accessToken", required = false) String accessToken,
                                                      HttpServletRequest request, 
                                                      HttpServletResponse response) {
        log.info("[회원 정보 조회] 요청 수신");

        try {
            if (accessToken == null || accessToken.isEmpty()) {
                log.warn("accessToken 쿠키가 없음");
                return new ApiResponse<>(0, "accessToken이 유효하지 않거나 없습니다.", null);
            }

            String email = jwtTokenProvider.getEmailFromToken(accessToken);
            
            if (jwtTokenProvider.isExpired(accessToken)) {
                log.warn("JWT 만료됨. 리프레시 토큰 검토 중...");
                accessToken = handleTokenRefresh(request, response, email);
                if (accessToken == null) {
                    return new ApiResponse<>(0, "세션이 만료되었습니다. 다시 로그인하세요.", null);
                }
            }

            Member member = findMemberByEmail(email);
            ProfileResponseDto responseDto = buildProfileResponse(member);
            
            log.info("회원 정보 조회 성공: {}", member.getEmail());
            return new ApiResponse<>(1, "회원 정보 조회 성공", responseDto);
            
        } catch (JwtTokenProvider.InvalidJwtException e) {
            log.error("JWT 파싱 실패: {}", e.getMessage());
            return new ApiResponse<>(0, "Invalid token", null);
        } catch (Exception e) {
            log.error("회원 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            return new ApiResponse<>(0, "회원 정보 조회 중 오류가 발생했습니다.", null);
        }
    }

    @PostMapping("logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        try {
            jwtTokenProvider.clearJwtCookies(response);
            log.info("로그아웃 성공");
            return new ApiResponse<>(1, "로그아웃 되었습니다.", null);
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            return new ApiResponse<>(0, "로그아웃 처리 중 오류가 발생했습니다.", null);
        }
    }

    private String validateAndExtractAccessToken(Map<String, String> requestBody) {
        String accessToken = requestBody.get("accessToken");
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("AccessToken is missing.");
        }
        return accessToken;
    }

    private Member findOrCreateMember(GoogleUserDto googleUser) {
        final String email = googleUser.getEmail();
        final MemberRole targetRole = ADMIN_EMAIL_ALLOWLIST.contains(email) ? MemberRole.ADMIN : MemberRole.STUDENT;

        return memberRepository.findByEmail(email)
                .map(existingMember -> {
                    if (existingMember.getRole() != targetRole) {
                        existingMember.setRole(targetRole);
                        Member updated = memberRepository.save(existingMember);
                        log.info("회원 권한 동기화: {} -> {}", email, targetRole.name());
                        return updated;
                    }
                    return existingMember;
                })
                .orElseGet(() -> {
                    Member newMember = new Member(googleUser);
                    newMember.setRole(targetRole);
                    Member savedMember = memberRepository.save(newMember);
                    log.info("새로운 회원 생성: {} ({})", savedMember.getEmail(), targetRole.name());
                    return savedMember;
                });
    }

    private String createNewAccessToken(Member member) {
        return JwtTokenProvider.createAccessToken(member);
    }

    private String handleRefreshToken(String refreshToken, Member member, HttpServletResponse response) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            log.info("유효한 리프레시 토큰 존재. 새로운 JWT 액세스 토큰 발급 중...");
            return null; // 기존 리프레시 토큰 재사용
        } else {
            log.info("리프레시 토큰 없음 또는 유효하지 않음. 새로운 JWT 토큰 발급...");
            return jwtTokenProvider.createRefreshToken(member);
        }
    }

    private String handleTokenRefresh(HttpServletRequest request, HttpServletResponse response, String email) {
        String refreshToken = jwtTokenProvider.getRefreshTokenFromCookie(request);
        
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            log.info("유효한 리프레시 토큰 확인. 새로운 JWT 액세스 토큰 발급 중...");
            Member member = findMemberByEmail(email);
            String newAccessToken = JwtTokenProvider.createAccessToken(member);
            jwtTokenProvider.setJwtCookie(response, newAccessToken, refreshToken);
            return newAccessToken;
        } else {
            log.error("리프레시 토큰이 없거나 유효하지 않음.");
            return null;
        }
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("회원 정보 없음: {}", email);
                    return new RuntimeException("회원 정보가 없습니다.");
                });
    }

    private ProfileResponseDto buildProfileResponse(Member member) {
        return ProfileResponseDto.builder()
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();
    }
}
