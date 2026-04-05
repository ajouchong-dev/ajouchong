package com.ajouchong.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RentalRecordReturnRequestDto {

    @NotBlank(message = "담당자 확인은 필수입니다.")
    private String managerConfirmation;
}

