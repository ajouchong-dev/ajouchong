package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.RentalItemResponseDto;
import com.ajouchong.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rental")
@RequiredArgsConstructor
public class RentalUserController {

    private final RentalService rentalService;

    @GetMapping("/items")
    public ApiResponse<List<RentalItemResponseDto>> getRentalItems() {
        return new ApiResponse<>(1, "대여 품목 조회 성공", rentalService.getActiveItems());
    }
}

