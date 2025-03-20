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

import java.io.IOException;

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

//    @PostMapping
//    public ApiResponse<NoticePostResponseDto> uploadNoticePost(
//            @ModelAttribute NoticePostAddFormDto requestDto,
//            @RequestHeader("Authorization") String authorizationHeader) throws IOException {
//
//        String token = authorizationHeader.substring(7);
//        NoticePostRequestDto noticePostRequestDto = requestDto.createNoticePostDto(jwtTokenProvider.getUserFromToken(token));
//        NoticePostResponseDto savedNoticePost = noticePostService.saveNoticePost(noticePostRequestDto, token);
//
//        return new ApiResponse<>(1, "게시글 업로드 성공", savedNoticePost);
//    }

    @PostMapping
    public ApiResponse<NoticePostResponseDto> uploadNoticePost(
            @ModelAttribute NoticePostAddFormDto requestDto,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws IOException {

        String token = null;
        Member member = null;

        // Authorization 헤더가 존재하면 토큰 처리
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                String email = jwtTokenProvider.getEmailFromToken(token);
                member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            } catch (Exception e) {
                System.out.println("유효하지 않은 토큰: " + e.getMessage());
            }
        }

        NoticePostRequestDto noticePostRequestDto = NoticePostRequestDto.builder()
                .author(member)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imageFiles(requestDto.getImageFiles()) // 이미지 파일만 처리
                .build();

        NoticePostResponseDto savedNoticePost = noticePostService.saveNoticePost(noticePostRequestDto, token);

        return new ApiResponse<>(1, "게시글 업로드 성공", savedNoticePost);
    }



    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNoticePost(@PathVariable Long id) {
        noticePostService.deleteNoticePost(id);
        return new ApiResponse<>(1, id + "번 게시글 삭제 성공", null);
    }
}
