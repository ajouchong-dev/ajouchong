package com.ajouchong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ProceedingDocumentResponseDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String fileUrl;
    private LocalDate meetingDate;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

