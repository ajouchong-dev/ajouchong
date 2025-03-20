package com.ajouchong.service;

import com.ajouchong.dto.request.AnswerRequestDto;
import com.ajouchong.dto.request.QnaPostRequestDto;
import com.ajouchong.dto.response.AnswerResponseDto;
import com.ajouchong.dto.response.QnaPostResponseDto;
import com.ajouchong.entity.Answer;
import com.ajouchong.entity.Member;
import com.ajouchong.entity.QnaLike;
import com.ajouchong.entity.QnaPost;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import com.ajouchong.repository.QnaLikeRepository;
import com.ajouchong.repository.QnaPostRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QnaPostService {
    private final QnaPostRepository qnaPostRepository;
    private final QnaLikeRepository qnaLikeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public QnaPostService(QnaPostRepository qnaPostRepository, QnaLikeRepository qnaLikeRepository, JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.qnaPostRepository = qnaPostRepository;
        this.qnaLikeRepository = qnaLikeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public QnaPostResponseDto createPost(QnaPostRequestDto requestDto, String author) {
        QnaPost post = new QnaPost();

        post.setQpAuthor(author);
        post.setQpTitle(requestDto.getQpTitle());
        post.setQpContent(requestDto.getQpContent());

        post.setQpUserLikeCnt(0);
        post.setQpHitCnt(0);
        post.setQpUpdateTime(LocalDateTime.now());
        post.setQpCreateTime(LocalDateTime.now());

        QnaPost savedPost = qnaPostRepository.save(post);

        return convertToQnaResponseDto(savedPost);
    }


    @Transactional
    public QnaPostResponseDto getPostById(Long id, String token) {
        QnaPost qnaPost = qnaPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "번 게시글을 찾을 수 없습니다."));

        qnaPost.setQpHitCnt(qnaPost.getQpHitCnt() + 1);
        qnaPostRepository.save(qnaPost);

        boolean likedByCurrentUser = false;
        if (token != null){ // 사용자 좋아요 여부 확인
            String email = jwtTokenProvider.getEmailFromToken(token);
            likedByCurrentUser = isUserLikedPost(id, email);
        }

        return new QnaPostResponseDto(qnaPost, likedByCurrentUser);
    }

    public List<QnaPostResponseDto> getAllPosts() {
        List<QnaPost> qnaPosts = qnaPostRepository.findAll(Sort.by(Sort.Direction.DESC, "qpCreateTime"));

        return qnaPosts.stream()
                .map(this::convertToQnaResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> toggleLike(Long postId, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<QnaLike> existingLike = qnaLikeRepository.findByMemberAndQnaPostId(member, postId);
        boolean isLiked;

        if (existingLike.isPresent()) {
            qnaLikeRepository.delete(existingLike.get());
            isLiked = false; // 좋아요 취소됨
        } else {
            QnaLike qnaLike = new QnaLike();
            qnaLike.setMember(member);
            qnaLike.setQnaPostId(postId);
            qnaLikeRepository.save(qnaLike);
            isLiked = true; // 좋아요 추가됨
        }

        // 좋아요 개수 업데이트
        long likeCount = qnaLikeRepository.countByQnaPostId(postId);
        QnaPost qnaPost = qnaPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(postId + "번 게시글을 찾을 수 없습니다."));
        qnaPost.setQpUserLikeCnt((int) likeCount);
        qnaPostRepository.save(qnaPost);

        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }


    @Transactional
    public boolean isUserLikedPost(Long postId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<QnaLike> existingLike = qnaLikeRepository.findByMemberAndQnaPostId(member, postId);

        return existingLike.isPresent();
    }

    @Transactional
    public void deletePost(Long postId) {
        QnaPost post = qnaPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(postId + "번 게시글을 찾을 수 없습니다."));

        qnaPostRepository.delete(post);
    }

    @Transactional
    public AnswerResponseDto addAnswer(Long postId, AnswerRequestDto requestDto) {
        QnaPost post = qnaPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(postId + "번 게시글을 찾을 수 없습니다."));

        Answer answer = post.getAnswer();

        if (answer != null) {
            answer.setContent(requestDto.getContent());
            answer.setUpdateTime(LocalDateTime.now());
        } else {
            answer = new Answer();
            answer.setContent(requestDto.getContent());
            answer.setQnaPost(post);
            answer.setCreateTime(LocalDateTime.now());
            answer.setUpdateTime(LocalDateTime.now());
            post.setAnswer(answer);
            post.setReplied(true);

        }

        qnaPostRepository.save(post);

        return new AnswerResponseDto(answer);
    }

    @Transactional
    public QnaPostResponseDto deleteAnswer(Long postId) {
        QnaPost post = qnaPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (post.getAnswer() != null) {
            post.setAnswer(null);
            post.setReplied(false);
            QnaPost updatedPost = qnaPostRepository.save(post);
            return new QnaPostResponseDto(updatedPost, false);
        } else {
            throw new IllegalArgumentException("해당 게시글에 답변이 없습니다.");
        }
    }

    public QnaPostResponseDto convertToQnaResponseDto(QnaPost qnaPost){
        return new QnaPostResponseDto(qnaPost, false);
    }

//    private String extractTokenFromHeader(HttpServletRequest request) {
//        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 반환
//        }
//        return null;
//    }
}