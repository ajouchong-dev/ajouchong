package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.PartnershipResponseDto;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.service.PartnershipService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/partnership")
public class PartnershipUserController {
    private final PartnershipService partnershipService;
    private final JwtTokenProvider jwtTokenProvider;

    public PartnershipUserController(PartnershipService partnershipService, JwtTokenProvider jwtTokenProvider) {
        this.partnershipService = partnershipService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ApiResponse<List<PartnershipResponseDto>> getAllPartnerships() {
        List<PartnershipResponseDto> partnerships = partnershipService.getLatestPartnerships();
        return new ApiResponse<>(1, "모든 제휴 백과 목록 조회 성공", partnerships);
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnershipResponseDto> getPartnershipById(@PathVariable Long id,
                                                                  @CookieValue(value = "accessToken", required = false) String token) {

        PartnershipResponseDto partnership = partnershipService.getPartnershipById(id, token);
        return new ApiResponse<>(1, id + "번 게시글 조회 성공", partnership);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> increaseLikeCount(@PathVariable Long id,
                                               @CookieValue(value = "accessToken", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        Map<String, Object> result = partnershipService.togglePsLike(id, token);
        boolean isLiked = (boolean) result.get("isLiked");
        long likeCount = (long) result.get("likeCount");

        String message = isLiked ? "번 게시글 좋아요 성공" : "번 게시글 좋아요 취소 성공";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("isLiked", isLiked);
        responseData.put("likeCount", likeCount);

        return new ApiResponse<>(1, id + message, responseData);
    }
}
