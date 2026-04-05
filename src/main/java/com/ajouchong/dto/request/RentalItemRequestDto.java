package com.ajouchong.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RentalItemRequestDto {

    @NotBlank(message = "품목명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotNull(message = "총 수량은 필수입니다.")
    @Min(value = 0, message = "총 수량은 0 이상이어야 합니다.")
    private Integer totalQuantity;

    @NotNull(message = "현재 수량은 필수입니다.")
    @Min(value = 0, message = "현재 수량은 0 이상이어야 합니다.")
    private Integer currentQuantity;

    private String imageUrl;
    private String note;

    @NotNull(message = "노출 순서는 필수입니다.")
    @Min(value = 1, message = "노출 순서는 1 이상이어야 합니다.")
    private Integer displayOrder;

    @NotNull(message = "활성 상태는 필수입니다.")
    private Boolean active;
}

