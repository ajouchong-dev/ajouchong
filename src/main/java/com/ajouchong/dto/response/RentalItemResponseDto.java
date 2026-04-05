package com.ajouchong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RentalItemResponseDto {
    private Long id;
    private String name;
    private String category;
    private Integer totalQuantity;
    private Integer currentQuantity;
    private String imageUrl;
    private String note;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

