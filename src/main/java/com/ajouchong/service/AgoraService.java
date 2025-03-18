package com.ajouchong.service;

import com.ajouchong.dto.request.AgoraRequestDto;
import com.ajouchong.dto.response.AgoraResponseDto;
import com.ajouchong.entity.Agora;
import com.ajouchong.entity.AgoraLike;
import com.ajouchong.entity.Member;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.AgoraLikeRepository;
import com.ajouchong.repository.AgoraRepository;
import com.ajouchong.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AgoraService {
    private final AgoraRepository agoraRepository;
    private final AgoraLikeRepository agoraLikeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public AgoraService(AgoraRepository agoraRepository, AgoraLikeRepository agoraLikeRepository, JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.agoraRepository = agoraRepository;
        this.agoraLikeRepository = agoraLikeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public AgoraResponseDto createAgoraPost(AgoraRequestDto requestDto, String author) {
        Agora agora = new Agora();

        agora.setAuthor(author);
        agora.setApTitle(requestDto.getApTitle());
        agora.setApContent(requestDto.getApContent());

        agora.setApUserLikeCount(0);
        agora.setApHitCount(0);

        Agora savedPost = agoraRepository.save(agora);

        return convertToAgoraResponseDto(savedPost, null);
    }

    @Transactional
    public AgoraResponseDto getAgoraById(Long postId, String token) {
        Agora agora = agoraRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(postId + "번 게시글을 찾을 수 없습니다."));

        agora.setApHitCount(agora.getApHitCount() + 1);
        agoraRepository.save(agora);

        String email = (token != null) ? jwtTokenProvider.getEmailFromToken(token) : null;
        return convertToAgoraResponseDto(agora, email);
    }

    @Transactional
    public boolean isUserLikedAgora(Long postId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<AgoraLike> existingLike = agoraLikeRepository.findByMemberAndAgoraId(member, postId);

        return existingLike.isPresent();
    }

    @Transactional(readOnly = true)
    public List<AgoraResponseDto> getAllPosts() {
        List<Agora> agoras = agoraRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime"));

        return agoras.stream()
                .map(agora -> new AgoraResponseDto(agora, false))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deletePost(Long postId) {
        Agora agora = agoraRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(postId + "번 게시글을 찾을 수 없습니다."));

        agoraRepository.delete(agora);
    }

    @Transactional
    public void approvePost(Long postId, String token) {
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효한 토큰이 필요합니다.");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Agora agora = agoraRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(postId + "번 게시글을 찾을 수 없습니다."));

        log.info(agora.getAuthor());
        log.info(agora.getApTitle());
        agora.approvePost();

        // 필수 필드가 null이 아닌지 확인
        if (agora.getAuthor() == null || agora.getApTitle() == null) {
            throw new RuntimeException("필수 데이터가 누락되었습니다.");
        }

        agoraRepository.save(agora);
    }


    @Transactional
    public Map<String, Object> toggleAgoraLike(Long postId, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<AgoraLike> exLike = agoraLikeRepository.findByMemberAndAgoraId(member, postId);
        boolean isLiked;

        if (exLike.isPresent()) {
            agoraLikeRepository.delete(exLike.get());
            isLiked = false;
        } else {
            AgoraLike agoraLike = new AgoraLike();
            agoraLike.setMember(member);
            agoraLike.setAgoraId(postId);
            agoraLikeRepository.save(agoraLike);
            isLiked = true;
        }

        // 좋아요 개수 업데이트
        long likeCount = agoraLikeRepository.countByAgoraId(postId);
        Agora agora = agoraRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(postId + "번 게시글을 찾을 수 없습니다."));
        agora.setApUserLikeCount((int) likeCount);
        agoraRepository.save(agora);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }

    @Transactional
    public AgoraResponseDto convertToAgoraResponseDto(Agora agora, String userEmail) {
        boolean likedByCurrentUser = false;
        if (userEmail != null) {
            likedByCurrentUser = isUserLikedAgora(agora.getAPostId(), userEmail);
        }
        return new AgoraResponseDto(agora, likedByCurrentUser);
    }

}
