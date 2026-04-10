package com.ajouchong.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProceedingCategoryRequestDto {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    private String description;

    @NotNull(message = "표시 순서는 필수입니다.")
    private Integer displayOrder;

    @NotNull(message = "활성 상태는 필수입니다.")
    private Boolean active;
}

