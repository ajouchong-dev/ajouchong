package com.ajouchong.service;

import com.ajouchong.dto.request.ProceedingCategoryRequestDto;
import com.ajouchong.dto.request.ProceedingDocumentRequestDto;
import com.ajouchong.dto.response.ProceedingCategoryResponseDto;
import com.ajouchong.dto.response.ProceedingDocumentResponseDto;
import com.ajouchong.entity.ProceedingCategory;
import com.ajouchong.entity.ProceedingDocument;
import com.ajouchong.repository.ProceedingCategoryRepository;
import com.ajouchong.repository.ProceedingDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProceedingService {

    private final ProceedingCategoryRepository categoryRepository;
    private final ProceedingDocumentRepository documentRepository;
    private final S3UploadService s3UploadService;

    public List<ProceedingCategoryResponseDto> getProceedingForUser() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(category -> toCategoryDto(category, true))
                .collect(Collectors.toList());
    }

    public List<ProceedingCategoryResponseDto> getAllCategoriesForAdmin() {
        return categoryRepository.findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(category -> toCategoryDto(category, false))
                .collect(Collectors.toList());
    }

    public List<ProceedingDocumentResponseDto> getAllDocumentsForAdmin() {
        return documentRepository.findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProceedingCategoryResponseDto createCategory(ProceedingCategoryRequestDto requestDto) {
        ProceedingCategory category = ProceedingCategory.builder()
                .name(requestDto.getName().trim())
                .description(normalizeNullableText(requestDto.getDescription()))
                .displayOrder(requestDto.getDisplayOrder())
                .active(requestDto.getActive())
                .build();

        return toCategoryDto(categoryRepository.save(category), false);
    }

    @Transactional
    public ProceedingCategoryResponseDto updateCategory(Long id, ProceedingCategoryRequestDto requestDto) {
        ProceedingCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의록 카테고리를 찾을 수 없습니다. ID: " + id));

        category.setName(requestDto.getName().trim());
        category.setDescription(normalizeNullableText(requestDto.getDescription()));
        category.setDisplayOrder(requestDto.getDisplayOrder());
        category.setActive(requestDto.getActive());

        return toCategoryDto(categoryRepository.save(category), false);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ProceedingCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의록 카테고리를 찾을 수 없습니다. ID: " + id));
        categoryRepository.delete(category);
    }

    @Transactional
    public ProceedingDocumentResponseDto createDocument(ProceedingDocumentRequestDto requestDto) {
        ProceedingCategory category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("회의록 카테고리를 찾을 수 없습니다. ID: " + requestDto.getCategoryId()));

        ProceedingDocument document = ProceedingDocument.builder()
                .category(category)
                .title(requestDto.getTitle().trim())
                .fileUrl(requestDto.getFileUrl().trim())
                .meetingDate(requestDto.getMeetingDate())
                .displayOrder(requestDto.getDisplayOrder())
                .active(requestDto.getActive())
                .build();

        return toDocumentDto(documentRepository.save(document));
    }

    @Transactional
    public ProceedingDocumentResponseDto updateDocument(Long id, ProceedingDocumentRequestDto requestDto) {
        ProceedingDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의록 문서를 찾을 수 없습니다. ID: " + id));
        ProceedingCategory category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("회의록 카테고리를 찾을 수 없습니다. ID: " + requestDto.getCategoryId()));

        document.setCategory(category);
        document.setTitle(requestDto.getTitle().trim());
        document.setFileUrl(requestDto.getFileUrl().trim());
        document.setMeetingDate(requestDto.getMeetingDate());
        document.setDisplayOrder(requestDto.getDisplayOrder());
        document.setActive(requestDto.getActive());

        return toDocumentDto(documentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(Long id) {
        ProceedingDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의록 문서를 찾을 수 없습니다. ID: " + id));
        documentRepository.delete(document);
    }

    public String uploadProceedingFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        boolean isPdf = filename.endsWith(".pdf") || contentType.contains("pdf");
        if (!isPdf) {
            throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
        }

        try {
            return s3UploadService.saveFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("회의록 파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private ProceedingCategoryResponseDto toCategoryDto(ProceedingCategory category, boolean userViewOnlyActiveDocs) {
        List<ProceedingDocument> documents = userViewOnlyActiveDocs
                ? documentRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAscIdAsc(category.getId())
                : documentRepository.findByCategoryIdOrderByDisplayOrderAscIdAsc(category.getId());

        return ProceedingCategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .documents(documents.stream().map(this::toDocumentDto).collect(Collectors.toList()))
                .build();
    }

    private ProceedingDocumentResponseDto toDocumentDto(ProceedingDocument document) {
        return ProceedingDocumentResponseDto.builder()
                .id(document.getId())
                .categoryId(document.getCategory().getId())
                .categoryName(document.getCategory().getName())
                .title(document.getTitle())
                .fileUrl(document.getFileUrl())
                .meetingDate(document.getMeetingDate())
                .displayOrder(document.getDisplayOrder())
                .active(document.getActive())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
