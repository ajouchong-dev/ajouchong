package com.ajouchong.service;

import com.ajouchong.dto.request.NoticePostRequestDto;
import com.ajouchong.dto.response.NoticePostResponseDto;
import com.ajouchong.entity.Member;
import com.ajouchong.entity.NoticeLike;
import com.ajouchong.entity.NoticePost;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import com.ajouchong.repository.NoticeLikeRepository;
import com.ajouchong.repository.NoticePostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticePostService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final NoticePostRepository noticePostRepository;
    private final S3UploadService s3UploadService;
    private final NoticeLikeRepository noticeLikeRepository;

    @Transactional
    public NoticePostResponseDto saveNoticePost(NoticePostRequestDto requestDto, String token) throws IOException {
        Member author = null;

        // 로그인된 사용자 정보 추출 (토큰이 있을 경우에만)
        if (token != null && !token.isBlank()) {
            try {
                String email = jwtTokenProvider.getEmailFromToken(token);
                author = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            } catch (Exception e) {
                log.debug("err");
            }
        }

        // 이미지 파일 처리
        List<String> imageUrls = new ArrayList<>();
        if (requestDto.getImageFiles() != null && !requestDto.getImageFiles().isEmpty()) {
            for (MultipartFile file : requestDto.getImageFiles()) {
                String image = s3UploadService.saveFile(file); // S3에 업로드 후 URL 반환
                imageUrls.add(image);
            }
        }

        NoticePost noticePost = requestDto.createNoticePost(imageUrls);
        noticePost.setImageUrls(imageUrls);
        noticePost.setAuthor(author);

        NoticePost savedNoticePost = noticePostRepository.save(noticePost);

        return convertToResponseDto(savedNoticePost, null);
    }

    @Transactional
    public List<NoticePostResponseDto> getLatestNoticePosts() {
        List<NoticePost> noticePosts = noticePostRepository.findAll(Sort.by(Sort.Direction.DESC, "npCreateTime"));

        return noticePosts.stream()
                .map(noticePost -> new NoticePostResponseDto(noticePost, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public NoticePostResponseDto getNoticePostWithHitIncrement(Long id, String token) {
        NoticePost noticePost = noticePostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(id + "번 게시글을 찾을 수 없습니다."));

        noticePost.setNpHitCnt(noticePost.getNpHitCnt() + 1);
        noticePostRepository.save(noticePost);

        String email = (token != null) ? jwtTokenProvider.getEmailFromToken(token) : null;
        return convertToResponseDto(noticePost, email);
    }

    @Transactional
    public void deleteNoticePost(Long id) {
        if (!noticePostRepository.existsById(id)) {
            throw new RuntimeException(id + "번 게시글을 찾을 수 없습니다.");
        }
        noticePostRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long postId, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<NoticeLike> existingLike = noticeLikeRepository.findByMemberAndNoticePostId(member, postId);
        boolean isLiked;

        if (existingLike.isPresent()) {
            noticeLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            NoticeLike noticeLike = new NoticeLike();
            noticeLike.setMember(member);
            noticeLike.setNoticePostId(postId);
            noticeLikeRepository.save(noticeLike);
            isLiked = true;
        }

        // 좋아요 개수 업데이트
        long likeCount = noticeLikeRepository.countByNoticePostId(postId);
        NoticePost noticePost = noticePostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(postId + "번 게시글을 찾을 수 없습니다."));
        noticePost.setNpUserLikeCnt((int) likeCount);
        noticePostRepository.save(noticePost);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }

    @Transactional
    public boolean isUserLikedPost(Long postId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<NoticeLike> existingLike = noticeLikeRepository.findByMemberAndNoticePostId(member, postId);

        return existingLike.isPresent();
    }

    @Transactional
    public NoticePostResponseDto convertToResponseDto(NoticePost noticePost, String email) {
        boolean likedByCurrentUser = false;
        if (email != null) {
            likedByCurrentUser = isUserLikedPost(noticePost.getNPostId(), email);
        }
        return new NoticePostResponseDto(noticePost, likedByCurrentUser);
    }

}

