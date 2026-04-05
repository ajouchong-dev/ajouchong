package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.PromotionPartnerRequestDto;
import com.ajouchong.dto.response.PromotionPartnerResponseDto;
import com.ajouchong.service.PromotionPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promotion")
@RequiredArgsConstructor
public class PromotionAdminController {

    private final PromotionPartnerService promotionPartnerService;

    @GetMapping
    public ApiResponse<List<PromotionPartnerResponseDto>> getAllPromotionPartners() {
        List<PromotionPartnerResponseDto> partners = promotionPartnerService.getAllPartnersForAdmin();
        return new ApiResponse<>(1, "제휴 항목 목록 조회 성공", partners);
    }

    @PostMapping
    public ApiResponse<PromotionPartnerResponseDto> createPromotionPartner(
            @Valid @RequestBody PromotionPartnerRequestDto requestDto) {
        PromotionPartnerResponseDto response = promotionPartnerService.createPartner(requestDto);
        return new ApiResponse<>(1, "제휴 항목 생성 성공", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<PromotionPartnerResponseDto> updatePromotionPartner(
            @PathVariable Long id,
            @Valid @RequestBody PromotionPartnerRequestDto requestDto) {
        PromotionPartnerResponseDto response = promotionPartnerService.updatePartner(id, requestDto);
        return new ApiResponse<>(1, "제휴 항목 수정 성공", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromotionPartner(@PathVariable Long id) {
        promotionPartnerService.deletePartner(id);
        return new ApiResponse<>(1, "제휴 항목 삭제 성공", null);
    }
}

