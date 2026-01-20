package com.ajouchong.service;

import com.ajouchong.dto.request.LinkRequestDto;
import com.ajouchong.dto.response.LinkResponseDto;
import com.ajouchong.entity.Link;
import com.ajouchong.repository.LinkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkService {
    
    private final LinkRepository linkRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
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
        List<Link> links = linkRepository.findAllByOrderByCreatedAtAsc();
        
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
        syncSequence();
    }
    
    private void syncSequence() {
        try {
            Long maxId = linkRepository.findMaxId().orElse(0L);
            
            // 시퀀스 이름을 동적으로 찾기
            String sequenceName = linkRepository.getSequenceName()
                    .orElse("links_id_seq");
            
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
                Long maxId = linkRepository.findMaxId().orElse(0L);
                // 대체 시퀀스 이름 시도
                String[] possibleSequences = {
                    "links_id_seq",
                    "link_id_seq"
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
}
