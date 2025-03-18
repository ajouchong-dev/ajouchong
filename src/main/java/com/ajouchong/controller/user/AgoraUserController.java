package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.AgoraRequestDto;
import com.ajouchong.dto.response.AgoraResponseDto;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.service.AgoraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/agora")
public class AgoraUserController {
    private final AgoraService agoraService;
    private final JwtTokenProvider jwtTokenProvider;

    public AgoraUserController(AgoraService agoraService, JwtTokenProvider jwtTokenProvider) {
        this.agoraService = agoraService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ApiResponse<AgoraResponseDto> createPost(@RequestBody AgoraRequestDto requestDto,
                                                    @CookieValue(value = "accessToken", required = false) String token) {

        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        String author = jwtTokenProvider.getNameFromToken(token);

        AgoraResponseDto responseDto = agoraService.createAgoraPost(requestDto, author);
        return new ApiResponse<>(1, "게시글이 게시되었습니다.", responseDto);
    }

    @GetMapping
    public ApiResponse<List<AgoraResponseDto>> getAllPosts() {
        List<AgoraResponseDto> allPosts = agoraService.getAllPosts();
        return new ApiResponse<>(1, "전체 게시글 목록 조회 성공", allPosts);
    }

    @GetMapping("{postId}")
    public ApiResponse<AgoraResponseDto> getPostById(@PathVariable Long postId,
                                                    @CookieValue(value = "accessToken", required = false) String token) {
        AgoraResponseDto agora = agoraService.getAgoraById(postId, token);
        return new ApiResponse<>(1, postId + "번 게시글 반환에 성공했습니다.", agora);
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Object>> incrementLikeCount(@PathVariable Long postId,
                                                @CookieValue(value = "accessToken", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        // log.info(token);
        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        Map<String, Object> result = agoraService.toggleAgoraLike(postId, token);
        boolean isLiked = (boolean) result.get("isLiked");
        long likeCount = (long) result.get("likeCount");

        String message = isLiked ? "번 게시글 좋아요 성공" : "번 게시글 좋아요 취소 성공";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("isLiked", isLiked);
        responseData.put("likeCount", likeCount);

        return new ApiResponse<>(1, postId + message, responseData);
    }
}
