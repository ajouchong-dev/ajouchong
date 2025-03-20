package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.service.AgoraService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/agora")
public class AgoraAdminController {
    private final AgoraService agoraService;
    private final JwtTokenProvider jwtTokenProvider;

    public AgoraAdminController(AgoraService agoraService, JwtTokenProvider jwtTokenProvider) {
        this.agoraService = agoraService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        agoraService.deletePost(postId);
        return new ApiResponse<>(1, "게시글이 삭제되었습니다.", null);
    }

    @PutMapping("/{postId}/approve")
    public ApiResponse<Void> approvePost(@PathVariable Long postId,
                                         @CookieValue(value = "accessToken", required = false) String token) {

        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }
        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 토큰입니다.", null);
        }

        agoraService.approvePost(postId, token);
        return new ApiResponse<>(1, "게시글이 승인되었습니다.", null);
    }
}