package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.entity.Member;
import com.ajouchong.entity.enumClass.MemberRole;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ApiResponse<List<Member>> getAllMembers() {
        return new ApiResponse<>(1, "전체 회원 조회 성공", memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ApiResponse<Optional<Member>> getMemberById(@PathVariable Long id,
                                             @CookieValue(value = "accessToken", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        Optional<Member> member = memberService.getMemberById(id);
        if (member.isEmpty()){
            return new ApiResponse<>(0, id + "번 회원 조회에 실패했습니다.", null);
        }

        return new ApiResponse<>(1, id + "번 회원 조회 성공", member);
    }

    @PutMapping("/{id}")
    public ApiResponse<Member> updateMember(@PathVariable Long id,
                                            @RequestBody Map<String, String> request,
                                            @CookieValue(value = "accessToken", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        String roleString = request.get("role");
        try {
            MemberRole role = MemberRole.valueOf(roleString);
            Member member = memberService.updateMemberRole(id, role);
            return new ApiResponse<>(1, id + "번 회원의 role이 변경되었습니다.", member);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(0, "잘못된 역할 값입니다.", null);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMember(@PathVariable Long id,
                                          @CookieValue(value = "accessToken", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        memberService.deleteMember(id);
        return new ApiResponse<>(1, id + "번 회원 삭제 성공", null);
    }

}
