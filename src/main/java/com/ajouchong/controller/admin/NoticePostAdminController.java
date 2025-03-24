package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.NoticePostAddFormDto;
import com.ajouchong.dto.request.NoticePostRequestDto;
import com.ajouchong.dto.response.NoticePostResponseDto;
import com.ajouchong.entity.Member;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import com.ajouchong.service.NoticePostService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ApiResponse<NoticePostResponseDto> uploadNoticePost(
            @ModelAttribute NoticePostAddFormDto requestDto,
            @CookieValue(value = "accessToken", required = false) String token) {

        if (token == null) {
            return new ApiResponse<>(0, "로그인이 필요합니다.", null);
        }

        if (!jwtTokenProvider.validateToken(token)) {
            return new ApiResponse<>(0, "유효하지 않은 JWT 토큰입니다.", null);
        }

        try {
            String email = jwtTokenProvider.getEmailFromToken(token);
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            NoticePostRequestDto noticePostRequestDto = NoticePostRequestDto.builder()
                    .author(member)
                    .title(requestDto.getTitle())
                    .content(requestDto.getContent())
                    .imageFiles(requestDto.getImageFiles()) // 이미지 파일만 처리
                    .build();

            NoticePostResponseDto savedNoticePost = noticePostService.saveNoticePost(noticePostRequestDto, token);

            return new ApiResponse<>(1, "게시글 업로드 성공", savedNoticePost);
        } catch (Exception e) {
            return new ApiResponse<>(0, "게시글 업로드 중 오류가 발생했습니다: " + e.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNoticePost(@PathVariable Long id) {
        noticePostService.deleteNoticePost(id);
        return new ApiResponse<>(1, id + "번 게시글 삭제 성공", null);
    }
}
