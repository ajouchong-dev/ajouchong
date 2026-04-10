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
                .active(requestDto.getActive() == null ? true : requestDto.getActive())
                .showLink(requestDto.getShowLink() == null ? true : requestDto.getShowLink())
                .build();

        Link savedLink = linkRepository.save(link);

        return toDto(savedLink);
    }

    public List<LinkResponseDto> getAllLinks() {
        List<Link> links = linkRepository.findAllByOrderByCreatedAtAsc();
        return links.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LinkResponseDto> getActiveLinks() {
        List<Link> links = linkRepository.findVisibleLinksOrderByCreatedAtAsc();
        return links.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public LinkResponseDto updateLink(Long id, LinkRequestDto requestDto) {
        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("링크를 찾을 수 없습니다. ID: " + id));

        link.setTitle(requestDto.getTitle());
        link.setLink(requestDto.getLink());
        if (requestDto.getActive() != null) {
            link.setActive(requestDto.getActive());
        }
        if (requestDto.getShowLink() != null) {
            link.setShowLink(requestDto.getShowLink());
        }

        Link updatedLink = linkRepository.save(link);
        return toDto(updatedLink);
    }

    @Transactional
    public void deleteLink(Long id) {
        Link link = linkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("링크를 찾을 수 없습니다. ID: " + id));

        linkRepository.delete(link);
        syncSequence();
    }

    private LinkResponseDto toDto(Link link) {
        return LinkResponseDto.builder()
                .id(link.getId())
                .title(link.getTitle())
                .link(link.getLink())
                .active(link.getActive() == null ? true : link.getActive())
                .showLink(link.getShowLink() == null ? true : link.getShowLink())
                .createdAt(link.getCreatedAt())
                .build();
    }

    private void syncSequence() {
        try {
            Long maxId = linkRepository.findMaxId().orElse(0L);

            String sequenceName = linkRepository.getSequenceName().orElse("links_id_seq");
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
                String[] possibleSequences = {"links_id_seq", "link_id_seq"};

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