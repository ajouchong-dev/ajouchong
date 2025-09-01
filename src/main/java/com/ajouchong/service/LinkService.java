package com.ajouchong.service;

import com.ajouchong.dto.request.LinkRequestDto;
import com.ajouchong.dto.response.LinkResponseDto;
import com.ajouchong.entity.Link;
import com.ajouchong.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkService {
    
    private final LinkRepository linkRepository;
    
    @Transactional
    public LinkResponseDto uploadLink(LinkRequestDto requestDto) {
        Link link = Link.builder()
                .title(requestDto.getTitle())
                .link(requestDto.getLink())
                .build();
        
        Link savedLink = linkRepository.save(link);
        
        return LinkResponseDto.builder()
                .id(savedLink.getId())
                .title(savedLink.getTitle())
                .link(savedLink.getLink())
                .createdAt(savedLink.getCreatedAt())
                .build();
    }
    
    public List<LinkResponseDto> getAllLinks() {
        List<Link> links = linkRepository.findAllByOrderByCreatedAtDesc();
        
        return links.stream()
                .map(link -> LinkResponseDto.builder()
                        .id(link.getId())
                        .title(link.getTitle())
                        .link(link.getLink())
                        .createdAt(link.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteLink(Long id) {
        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("링크를 찾을 수 없습니다. ID: " + id));
        
        linkRepository.delete(link);
    }
}
