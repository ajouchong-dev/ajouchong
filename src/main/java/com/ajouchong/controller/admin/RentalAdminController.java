package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.RentalItemRequestDto;
import com.ajouchong.dto.request.RentalRecordCreateRequestDto;
import com.ajouchong.dto.request.RentalRecordReturnRequestDto;
import com.ajouchong.dto.response.RentalItemResponseDto;
import com.ajouchong.dto.response.RentalRecordResponseDto;
import com.ajouchong.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rental")
@RequiredArgsConstructor
public class RentalAdminController {

    private final RentalService rentalService;

    @GetMapping("/items")
    public ApiResponse<List<RentalItemResponseDto>> getAllItems() {
        return new ApiResponse<>(1, "대여 품목 목록 조회 성공", rentalService.getAllItemsForAdmin());
    }

    @PostMapping("/items")
    public ApiResponse<RentalItemResponseDto> createItem(@Valid @RequestBody RentalItemRequestDto requestDto) {
        return new ApiResponse<>(1, "대여 품목 등록 성공", rentalService.createItem(requestDto));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<RentalItemResponseDto> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody RentalItemRequestDto requestDto) {
        return new ApiResponse<>(1, "대여 품목 수정 성공", rentalService.updateItem(id, requestDto));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        rentalService.deleteItem(id);
        return new ApiResponse<>(1, "대여 품목 삭제 성공", null);
    }

    @PatchMapping("/items/{id}/quantity")
    public ApiResponse<RentalItemResponseDto> adjustItemQuantity(
            @PathVariable Long id,
            @RequestParam Integer delta) {
        return new ApiResponse<>(1, "현재 수량 조정 성공", rentalService.adjustCurrentQuantity(id, delta));
    }

    @PostMapping("/items/upload-image")
    public ApiResponse<Map<String, String>> uploadItemImage(@RequestPart("file") MultipartFile file) {
        String imageUrl = rentalService.uploadRentalItemImage(file);
        return new ApiResponse<>(1, "이미지 업로드 성공", Map.of("imageUrl", imageUrl));
    }

    @GetMapping("/records")
    public ApiResponse<List<RentalRecordResponseDto>> getAllRecords() {
        return new ApiResponse<>(1, "대여 명부 조회 성공", rentalService.getAllRecordsForAdmin());
    }

    @PostMapping("/records")
    public ApiResponse<RentalRecordResponseDto> createRecord(
            @Valid @RequestBody RentalRecordCreateRequestDto requestDto) {
        return new ApiResponse<>(1, "대여 기록 등록 성공", rentalService.createRecord(requestDto));
    }

    @PatchMapping("/records/{id}/return")
    public ApiResponse<RentalRecordResponseDto> returnRecord(
            @PathVariable Long id,
            @Valid @RequestBody RentalRecordReturnRequestDto requestDto) {
        return new ApiResponse<>(1, "반납 처리 성공", rentalService.markAsReturned(id, requestDto));
    }
}
