package com.ajouchong.service;

import com.ajouchong.dto.request.NoticePostRequestDto;
import com.ajouchong.dto.request.NoticePostUpdateFormDto;
import com.ajouchong.dto.response.NoticePostResponseDto;
import com.ajouchong.entity.Member;
import com.ajouchong.entity.NoticeLike;
import com.ajouchong.entity.NoticePost;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import com.ajouchong.repository.NoticeLikeRepository;
import com.ajouchong.repository.NoticePostRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public NoticePostResponseDto saveNoticePost(NoticePostRequestDto requestDto, String token) throws IOException {
        Member author = null;

        if (token != null && !token.isBlank()) {
            try {
                String email = jwtTokenProvider.getEmailFromToken(token);
                author = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            } catch (Exception e) {
                log.debug("failed to extract author from token", e);
            }
        }

        List<String> imageUrls = new ArrayList<>();
        if (requestDto.getImageFiles() != null && !requestDto.getImageFiles().isEmpty()) {
            for (MultipartFile file : requestDto.getImageFiles()) {
                String image = s3UploadService.saveFile(file);
                imageUrls.add(image);
            }
        }

        NoticePost noticePost = requestDto.createNoticePost(imageUrls);
        noticePost.setImageUrls(imageUrls);
        noticePost.setAuthor(author);

        NoticePost savedNoticePost = noticePostRepository.save(noticePost);
        return convertToResponseDto(savedNoticePost, null);
    }

    @Transactional(readOnly = true)
    public List<NoticePostResponseDto> getLatestNoticePosts() {
        List<NoticePost> noticePosts = noticePostRepository.findAll(Sort.by(Sort.Direction.DESC, "npCreateTime"));

        return noticePosts.stream()
                .map(noticePost -> new NoticePostResponseDto(noticePost, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public NoticePostResponseDto getNoticePostWithHitIncrement(Long id, String token) {
        NoticePost noticePost = findNoticePostById(id);

        noticePost.setNpHitCnt(noticePost.getNpHitCnt() + 1);
        noticePostRepository.save(noticePost);

        String email = (token != null) ? jwtTokenProvider.getEmailFromToken(token) : null;
        return convertToResponseDto(noticePost, email);
    }

    @Transactional(readOnly = true)
    public NoticePostResponseDto getNoticePostWithoutHitIncrement(Long id, String token) {
        NoticePost noticePost = findNoticePostById(id);
        String email = (token != null) ? jwtTokenProvider.getEmailFromToken(token) : null;
        return convertToResponseDto(noticePost, email);
    }

    @Transactional
    public NoticePostResponseDto updateNoticePost(Long id, NoticePostUpdateFormDto requestDto) throws IOException {
        NoticePost noticePost = findNoticePostById(id);

        noticePost.setNpTitle(requestDto.getTitle());
        noticePost.setNpContent(requestDto.getContent());

        if (requestDto.getImageFiles() != null && !requestDto.getImageFiles().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : requestDto.getImageFiles()) {
                String imageUrl = s3UploadService.saveFile(file);
                imageUrls.add(imageUrl);
            }
            noticePost.setImageUrls(imageUrls);
        }

        NoticePost updatedNoticePost = noticePostRepository.save(noticePost);
        return convertToResponseDto(updatedNoticePost, null);
    }

    @Transactional
    public void deleteNoticePost(Long id) {
        if (!noticePostRepository.existsById(id)) {
            throw new RuntimeException(id + "번 게시글을 찾을 수 없습니다.");
        }

        long likeCount = noticeLikeRepository.countByNoticePostId(id);
        noticeLikeRepository.deleteByNoticePostId(id);
        if (likeCount > 0) {
            log.debug("게시글 {}번의 좋아요 {}개 삭제", id, likeCount);
        }

        noticePostRepository.deleteById(id);
        syncSequence();
    }

    private void syncSequence() {
        try {
            Long maxId = noticePostRepository.findMaxId().orElse(0L);

            String sequenceName = noticePostRepository.getSequenceName()
                    .orElse("notice_post_n_post_id_seq");

            if (sequenceName.contains(".")) {
                sequenceName = sequenceName.substring(sequenceName.indexOf(".") + 1);
            }

            String sql = "SELECT setval(?, ?, true)";
            entityManager.createNativeQuery(sql)
                    .setParameter(1, sequenceName)
                    .setParameter(2, maxId)
                    .getSingleResult();

            log.info("시퀀스 동기화 완료: 시퀀스={}, 최대 ID={}, 다음 ID={}", sequenceName, maxId, maxId + 1);
        } catch (Exception e) {
            log.error("시퀀스 동기화 실패: {}", e.getMessage(), e);
            try {
                Long maxId = noticePostRepository.findMaxId().orElse(0L);
                String[] possibleSequences = {
                        "notice_post_n_post_id_seq",
                        "noticepost_n_post_id_seq",
                        "notice_post_npostid_seq"
                };

                for (String seqName : possibleSequences) {
                    try {
                        String sql = "SELECT setval(?, ?, true)";
                        entityManager.createNativeQuery(sql)
                                .setParameter(1, seqName)
                                .setParameter(2, maxId)
                                .getSingleResult();
                        log.info("시퀀스 동기화 성공 (대체 시퀀스): {}, 다음 ID={}", seqName, maxId + 1);
                        return;
                    } catch (Exception ex) {
                        log.debug("시퀀스 이름 '{}' 시도 실패: {}", seqName, ex.getMessage());
                    }
                }
            } catch (Exception ex2) {
                log.error("대체 시퀀스 동기화도 실패: {}", ex2.getMessage());
            }
        }
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

    private NoticePost findNoticePostById(Long id) {
        return noticePostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(id + "번 게시글을 찾을 수 없습니다."));
    }
}
