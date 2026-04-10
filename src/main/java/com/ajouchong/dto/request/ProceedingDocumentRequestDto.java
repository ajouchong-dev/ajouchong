package com.ajouchong.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProceedingDocumentRequestDto {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "문서 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문서 URL은 필수입니다.")
    private String fileUrl;

    private LocalDate meetingDate;

    @NotNull(message = "표시 순서는 필수입니다.")
    private Integer displayOrder;

    @NotNull(message = "활성 상태는 필수입니다.")
    private Boolean active;
}

