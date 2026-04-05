package com.ajouchong.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromotionPartnerRequestDto {

    @NotBlank(message = "업체명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "혜택 내용은 필수입니다.")
    private String benefit;

    @NotBlank(message = "위치는 필수입니다.")
    private String location;

    private String homepageUrl;
    private String note;

    @NotNull(message = "노출 순서는 필수입니다.")
    private Integer displayOrder;

    @NotNull(message = "활성 상태는 필수입니다.")
    private Boolean active;
}

