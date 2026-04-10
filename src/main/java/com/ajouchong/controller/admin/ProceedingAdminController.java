package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.ProceedingCategoryRequestDto;
import com.ajouchong.dto.request.ProceedingDocumentRequestDto;
import com.ajouchong.dto.response.ProceedingCategoryResponseDto;
import com.ajouchong.dto.response.ProceedingDocumentResponseDto;
import com.ajouchong.service.ProceedingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/proceeding")
@RequiredArgsConstructor
public class ProceedingAdminController {

    private final ProceedingService proceedingService;

    @GetMapping("/categories")
    public ApiResponse<List<ProceedingCategoryResponseDto>> getAllCategories() {
        return new ApiResponse<>(1, "회의록 카테고리 목록 조회 성공", proceedingService.getAllCategoriesForAdmin());
    }

    @PostMapping("/categories")
    public ApiResponse<ProceedingCategoryResponseDto> createCategory(
            @Valid @RequestBody ProceedingCategoryRequestDto requestDto) {
        return new ApiResponse<>(1, "회의록 카테고리 생성 성공", proceedingService.createCategory(requestDto));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<ProceedingCategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ProceedingCategoryRequestDto requestDto) {
        return new ApiResponse<>(1, "회의록 카테고리 수정 성공", proceedingService.updateCategory(id, requestDto));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        proceedingService.deleteCategory(id);
        return new ApiResponse<>(1, "회의록 카테고리 삭제 성공", null);
    }

    @GetMapping("/documents")
    public ApiResponse<List<ProceedingDocumentResponseDto>> getAllDocuments() {
        return new ApiResponse<>(1, "회의록 문서 목록 조회 성공", proceedingService.getAllDocumentsForAdmin());
    }

    @PostMapping("/documents")
    public ApiResponse<ProceedingDocumentResponseDto> createDocument(
            @Valid @RequestBody ProceedingDocumentRequestDto requestDto) {
        return new ApiResponse<>(1, "회의록 문서 생성 성공", proceedingService.createDocument(requestDto));
    }

    @PutMapping("/documents/{id}")
    public ApiResponse<ProceedingDocumentResponseDto> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody ProceedingDocumentRequestDto requestDto) {
        return new ApiResponse<>(1, "회의록 문서 수정 성공", proceedingService.updateDocument(id, requestDto));
    }

    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        proceedingService.deleteDocument(id);
        return new ApiResponse<>(1, "회의록 문서 삭제 성공", null);
    }

    @PostMapping("/documents/upload-file")
    public ApiResponse<Map<String, String>> uploadDocumentFile(@RequestPart("file") MultipartFile file) {
        String fileUrl = proceedingService.uploadProceedingFile(file);
        return new ApiResponse<>(1, "회의록 파일 업로드 성공", Map.of("fileUrl", fileUrl));
    }
}
