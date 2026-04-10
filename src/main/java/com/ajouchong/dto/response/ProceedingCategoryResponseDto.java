package com.ajouchong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProceedingCategoryResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProceedingDocumentResponseDto> documents;
}

