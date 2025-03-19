package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.NoticePostResponseDto;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.service.NoticePostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/notice")
public class NoticePostUserController {
    private final NoticePostService noticePostService;
    private final JwtTokenProvider jwtTokenProvider;

    public NoticePostUserController(NoticePostService noticePostService, JwtTokenProvider jwtTokenProvider) {
        this.noticePostService = noticePostService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    public ApiResponse<List<NoticePostResponseDto>> getAllNoticePosts() {
        List<NoticePostResponseDto> allPosts = noticePostService.getLatestNoticePosts();
        return new ApiResponse<>(1, "모든 게시글 조회 성공", allPosts);
    }

    // 특정 게시물 조회
    @GetMapping("/{id}")
    public ApiResponse<NoticePostResponseDto> getNoticePostById(
            @PathVariable Long id,
            @CookieValue(value = "accessToken", required = false) String token) {

        NoticePostResponseDto post = noticePostService.getNoticePostWithHitIncrement(id, token);

        return new ApiResponse<>(1, id + "번 게시글 조회 성공", post);
    }


    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable Long id,
                                       @CookieValue(value = "accessToken", required = false) String token) {

        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        Map<String, Object> result = noticePostService.toggleLike(id, token);
        boolean isLiked = (boolean) result.get("isLiked");
        long likeCount = (long) result.get("likeCount");

        String message = isLiked ? "번 게시글 좋아요 성공" : "번 게시글 좋아요 취소 성공";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("isLiked", isLiked);
        responseData.put("likeCount", likeCount);

        return new ApiResponse<>(1, id + message,  responseData);
    }

}
