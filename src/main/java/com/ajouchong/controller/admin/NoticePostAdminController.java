package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.NoticePostAddFormDto;
import com.ajouchong.dto.request.NoticePostRequestDto;
import com.ajouchong.dto.request.NoticePostUpdateFormDto;
import com.ajouchong.dto.response.NoticePostResponseDto;
import com.ajouchong.entity.Member;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import com.ajouchong.service.NoticePostService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/admin/notice")
public class NoticePostAdminController {
    private final NoticePostService noticePostService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public NoticePostAdminController(NoticePostService noticePostService, JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.noticePostService = noticePostService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public ApiResponse<List<NoticePostResponseDto>> getAllNoticePosts() {
        List<NoticePostResponseDto> posts = noticePostService.getLatestNoticePosts();
        return new ApiResponse<>(1, "공지사항 목록 조회 성공", posts);
    }

    @GetMapping("/{id}")
    public ApiResponse<NoticePostResponseDto> getNoticePostById(
            @PathVariable Long id,
            @CookieValue(value = "accessToken", required = false) String token) {
        NoticePostResponseDto post = noticePostService.getNoticePostWithoutHitIncrement(id, token);
        return new ApiResponse<>(1, id + "번 공지사항 조회 성공", post);
    }

    @PostMapping
    public ApiResponse<NoticePostResponseDto> uploadNoticePost(
            @ModelAttribute NoticePostAddFormDto requestDto,
            @CookieValue(value = "accessToken", required = false) String token) {

        ApiResponse<Void> authError = validateToken(token);
        if (authError != null) {
            return new ApiResponse<>(authError.getCode(), authError.getMessage(), null);
        }

        try {
            String email = jwtTokenProvider.getEmailFromToken(token);
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            NoticePostRequestDto noticePostRequestDto = NoticePostRequestDto.builder()
                    .author(member)
                    .title(requestDto.getTitle())
                    .content(requestDto.getContent())
                    .imageFiles(requestDto.getImageFiles())
                    .build();

            NoticePostResponseDto savedNoticePost = noticePostService.saveNoticePost(noticePostRequestDto, token);
            return new ApiResponse<>(1, "공지사항 등록 성공", savedNoticePost);
        } catch (Exception e) {
            return new ApiResponse<>(0, "공지사항 등록 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<NoticePostResponseDto> updateNoticePost(
            @PathVariable Long id,
            @ModelAttribute NoticePostUpdateFormDto requestDto,
            @CookieValue(value = "accessToken", required = false) String token) {

        ApiResponse<Void> authError = validateToken(token);
        if (authError != null) {
            return new ApiResponse<>(authError.getCode(), authError.getMessage(), null);
        }

        try {
            NoticePostResponseDto updatedNoticePost = noticePostService.updateNoticePost(id, requestDto);
            return new ApiResponse<>(1, id + "번 공지사항 수정 성공", updatedNoticePost);
        } catch (Exception e) {
            return new ApiResponse<>(0, "공지사항 수정 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNoticePost(@PathVariable Long id) {
        noticePostService.deleteNoticePost(id);
        return new ApiResponse<>(1, id + "번 공지사항 삭제 성공", null);
    }

    private ApiResponse<Void> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        return null;
    }
}
