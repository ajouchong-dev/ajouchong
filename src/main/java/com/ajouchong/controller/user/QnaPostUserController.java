package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.QnaPostRequestDto;
import com.ajouchong.dto.response.QnaPostResponseDto;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.service.QnaPostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/qna")
public class QnaPostUserController {
    private final QnaPostService qnaPostService;
    private final JwtTokenProvider jwtTokenProvider;

    public QnaPostUserController(QnaPostService qnaPostService, JwtTokenProvider jwtTokenProvider) {
        this.qnaPostService = qnaPostService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ApiResponse<QnaPostResponseDto> createPost(@RequestBody QnaPostRequestDto requestDto,
                                                      @CookieValue(value = "accessToken", required = false) String token) {
        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        String author = jwtTokenProvider.getNameFromToken(token);

        QnaPostResponseDto responseDto = qnaPostService.createPost(requestDto, author);
        return new ApiResponse<>(1, "게시글이 게시되었습니다.", responseDto);
    }

    @GetMapping
    public ApiResponse<List<QnaPostResponseDto>> getAllPosts() {
        List<QnaPostResponseDto> allPosts = qnaPostService.getAllPosts();
        return new ApiResponse<>(1, "전체 게시글 목록 조회 성공", allPosts);
    }

    @GetMapping("{postId}")
    public ApiResponse<QnaPostResponseDto> getPostById(@PathVariable Long postId,
                                                       @CookieValue(value = "accessToken", required = false) String token) {
        QnaPostResponseDto responseDto = qnaPostService.getPostById(postId, token);
        return new ApiResponse<>(1, postId +"번 게시글 반환에 성공했습니다.", responseDto);
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Object>> incrementLikeCount(
            @PathVariable Long postId,
            @CookieValue(value = "accessToken", required = false) String token) {

        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        log.info(token);
        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        Map<String, Object> result = qnaPostService.toggleLike(postId, token);
        boolean isLiked = (boolean) result.get("isLiked");
        long likeCount = (long) result.get("likeCount");

        String message = isLiked ? "번 게시글 좋아요 성공" : "번 게시글 좋아요 취소 성공";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("isLiked", isLiked);
        responseData.put("likeCount", likeCount);

        return new ApiResponse<>(1, postId + message, responseData);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("Authorization Header: {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        log.warn("토큰이 존재하지 않거나 형식이 올바르지 않습니다.");
        return null;
    }
}
