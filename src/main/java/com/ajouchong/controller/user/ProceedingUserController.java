package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.ProceedingCategoryResponseDto;
import com.ajouchong.service.ProceedingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proceeding")
@RequiredArgsConstructor
public class ProceedingUserController {

    private final ProceedingService proceedingService;

    @GetMapping
    public ApiResponse<List<ProceedingCategoryResponseDto>> getProceeding() {
        return new ApiResponse<>(1, "회의록 목록 조회 성공", proceedingService.getProceedingForUser());
    }
}

