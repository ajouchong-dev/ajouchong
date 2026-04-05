package com.ajouchong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PromotionPartnerResponseDto {
    private Long id;
    private String name;
    private String category;
    private String benefit;
    private String location;
    private String homepageUrl;
    private String note;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

