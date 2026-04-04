package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.PromotionPartnerResponseDto;
import com.ajouchong.service.PromotionPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promotion")
@RequiredArgsConstructor
public class PromotionUserController {

    private final PromotionPartnerService promotionPartnerService;

    @GetMapping
    public ApiResponse<List<PromotionPartnerResponseDto>> getActivePromotionPartners() {
        List<PromotionPartnerResponseDto> partners = promotionPartnerService.getActivePartners();
        return new ApiResponse<>(1, "제휴 항목 조회 성공", partners);
    }
}

