package com.ajouchong.service;

import com.ajouchong.dto.request.RulePostRequestDto;
import com.ajouchong.dto.response.RulePostResponseDto;
import com.ajouchong.entity.RulePost;
import com.ajouchong.entity.enumClass.RuleType;
import com.ajouchong.repository.RulePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RulePostService {
    private final RulePostRepository rulePostRepository;

    @Transactional
    public RulePostResponseDto createRulePost(RulePostRequestDto requestDto, RuleType type) {
        RulePost rulePost = new RulePost();
        rulePost.setRpTitle(requestDto.getRpTitle());
        rulePost.setRpContent(requestDto.getRpContent());
        rulePost.setAttachmentUrl(requestDto.getAttachmentUrl());
        rulePost.setType(type);
        rulePost.setRpUserLikeCnt(0);
        rulePost.setRpUserLikeCnt(0);
        rulePostRepository.save(rulePost);
        return new RulePostResponseDto(rulePost);
    }

    @Transactional(readOnly = true)
    public List<RulePostResponseDto> getAllOfficialPosts() {
        return rulePostRepository.findByTypeOrderByRpCreateTimeDesc(RuleType.OFFICIAL).stream()
                .map(RulePostResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RulePostResponseDto> getAllDetailPosts() {
        return rulePostRepository.findByTypeOrderByRpCreateTimeDesc(RuleType.DETAIL).stream()
                .map(RulePostResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public RulePostResponseDto getRulePostById(Long id) {   //id로 게시글 조회
        RulePost rulePost = rulePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "번 게시글을 찾을 수 없습니다."));
        rulePost.incrementHitCount();
        return new RulePostResponseDto(rulePost);
    }

    @Transactional
    public void increaseLikeCount(Long id) {
        RulePost rulePost = rulePostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(id + "번 게시글을 찾을 수 없습니다."));
        rulePost.incrementUserLikeCount();
        rulePostRepository.save(rulePost);
    }

    @Transactional
    public void deleteRulePost(Long id) {
        RulePost rulePost = rulePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        rulePostRepository.delete(rulePost);
    }

}
