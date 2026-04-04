package com.ajouchong.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RentalRecordCreateRequestDto {

    @NotNull(message = "대여 품목은 필수입니다.")
    private Long rentalItemId;

    @NotNull(message = "대여 수량은 필수입니다.")
    @Min(value = 1, message = "대여 수량은 1 이상이어야 합니다.")
    private Integer quantity;

    @NotBlank(message = "담당자 이름은 필수입니다.")
    private String managerName;

    @NotNull(message = "대여 일자는 필수입니다.")
    private LocalDate rentalDate;

    @NotBlank(message = "대여자 이름은 필수입니다.")
    private String borrowerName;

    @NotBlank(message = "대여자 학과는 필수입니다.")
    private String borrowerDepartment;

    @NotBlank(message = "대여자 학번은 필수입니다.")
    private String borrowerStudentId;

    @NotBlank(message = "대여자 전화번호는 필수입니다.")
    private String borrowerPhone;

    @NotBlank(message = "손해 배상 동의 서명은 필수입니다.")
    private String borrowerSignature;

    private String note;
}

