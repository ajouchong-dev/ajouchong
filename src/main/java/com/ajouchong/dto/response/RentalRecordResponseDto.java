package com.ajouchong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RentalRecordResponseDto {
    private Long id;
    private Long rentalItemId;
    private String rentalItemName;
    private Integer quantity;
    private String managerName;
    private LocalDate rentalDate;
    private String borrowerName;
    private String borrowerDepartment;
    private String borrowerStudentId;
    private String borrowerPhone;
    private String borrowerSignature;
    private LocalDateTime returnedAt;
    private String managerConfirmation;
    private String note;
    private LocalDateTime createdAt;
}

