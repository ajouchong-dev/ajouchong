package com.ajouchong.service;

import com.ajouchong.dto.request.PromotionPartnerRequestDto;
import com.ajouchong.dto.response.PromotionPartnerResponseDto;
import com.ajouchong.entity.PromotionPartner;
import com.ajouchong.repository.PromotionPartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionPartnerService {

    private final PromotionPartnerRepository promotionPartnerRepository;

    public List<PromotionPartnerResponseDto> getActivePartners() {
        return promotionPartnerRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PromotionPartnerResponseDto> getAllPartnersForAdmin() {
        return promotionPartnerRepository.findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PromotionPartnerResponseDto createPartner(PromotionPartnerRequestDto requestDto) {
        PromotionPartner partner = PromotionPartner.builder()
                .name(requestDto.getName())
                .category(requestDto.getCategory())
                .benefit(requestDto.getBenefit())
                .location(requestDto.getLocation())
                .homepageUrl(requestDto.getHomepageUrl())
                .note(requestDto.getNote())
                .displayOrder(requestDto.getDisplayOrder())
                .active(requestDto.getActive())
                .build();

        return toDto(promotionPartnerRepository.save(partner));
    }

    @Transactional
    public PromotionPartnerResponseDto updatePartner(Long id, PromotionPartnerRequestDto requestDto) {
        PromotionPartner partner = promotionPartnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제휴 항목을 찾을 수 없습니다. ID: " + id));

        partner.setName(requestDto.getName());
        partner.setCategory(requestDto.getCategory());
        partner.setBenefit(requestDto.getBenefit());
        partner.setLocation(requestDto.getLocation());
        partner.setHomepageUrl(requestDto.getHomepageUrl());
        partner.setNote(requestDto.getNote());
        partner.setDisplayOrder(requestDto.getDisplayOrder());
        partner.setActive(requestDto.getActive());

        return toDto(promotionPartnerRepository.save(partner));
    }

    @Transactional
    public void deletePartner(Long id) {
        PromotionPartner partner = promotionPartnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("제휴 항목을 찾을 수 없습니다. ID: " + id));
        promotionPartnerRepository.delete(partner);
    }

    private PromotionPartnerResponseDto toDto(PromotionPartner partner) {
        return PromotionPartnerResponseDto.builder()
                .id(partner.getId())
                .name(partner.getName())
                .category(partner.getCategory())
                .benefit(partner.getBenefit())
                .location(partner.getLocation())
                .homepageUrl(partner.getHomepageUrl())
                .note(partner.getNote())
                .displayOrder(partner.getDisplayOrder())
                .active(partner.getActive())
                .createdAt(partner.getCreatedAt())
                .updatedAt(partner.getUpdatedAt())
                .build();
    }
}

