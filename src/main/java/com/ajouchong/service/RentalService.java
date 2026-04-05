package com.ajouchong.service;

import com.ajouchong.dto.request.RentalItemRequestDto;
import com.ajouchong.dto.request.RentalRecordCreateRequestDto;
import com.ajouchong.dto.request.RentalRecordReturnRequestDto;
import com.ajouchong.dto.response.RentalItemResponseDto;
import com.ajouchong.dto.response.RentalRecordResponseDto;
import com.ajouchong.entity.RentalItem;
import com.ajouchong.entity.RentalRecord;
import com.ajouchong.repository.RentalItemRepository;
import com.ajouchong.repository.RentalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {

    private final RentalItemRepository rentalItemRepository;
    private final RentalRecordRepository rentalRecordRepository;
    private final S3UploadService s3UploadService;

    public List<RentalItemResponseDto> getActiveItems() {
        return rentalItemRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    public List<RentalItemResponseDto> getAllItemsForAdmin() {
        return rentalItemRepository.findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RentalItemResponseDto createItem(RentalItemRequestDto requestDto) {
        validateItemQuantities(requestDto.getCurrentQuantity(), requestDto.getTotalQuantity());

        RentalItem item = RentalItem.builder()
                .name(requestDto.getName())
                .category(requestDto.getCategory())
                .totalQuantity(requestDto.getTotalQuantity())
                .currentQuantity(requestDto.getCurrentQuantity())
                .imageUrl(requestDto.getImageUrl())
                .note(requestDto.getNote())
                .displayOrder(requestDto.getDisplayOrder())
                .active(requestDto.getActive())
                .build();

        return toItemDto(rentalItemRepository.save(item));
    }

    @Transactional
    public RentalItemResponseDto updateItem(Long id, RentalItemRequestDto requestDto) {
        validateItemQuantities(requestDto.getCurrentQuantity(), requestDto.getTotalQuantity());
        RentalItem item = rentalItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대여 품목을 찾을 수 없습니다. ID: " + id));

        item.setName(requestDto.getName());
        item.setCategory(requestDto.getCategory());
        item.setTotalQuantity(requestDto.getTotalQuantity());
        item.setCurrentQuantity(requestDto.getCurrentQuantity());
        item.setImageUrl(requestDto.getImageUrl());
        item.setNote(requestDto.getNote());
        item.setDisplayOrder(requestDto.getDisplayOrder());
        item.setActive(requestDto.getActive());

        return toItemDto(rentalItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        RentalItem item = rentalItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대여 품목을 찾을 수 없습니다. ID: " + id));
        rentalItemRepository.delete(item);
    }

    @Transactional
    public RentalItemResponseDto adjustCurrentQuantity(Long id, Integer delta) {
        if (delta == null || delta == 0) {
            throw new IllegalArgumentException("수량 변경값(delta)은 0이 아니어야 합니다.");
        }

        RentalItem item = rentalItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대여 품목을 찾을 수 없습니다. ID: " + id));

        int next = item.getCurrentQuantity() + delta;
        if (next < 0) {
            throw new IllegalArgumentException("현재 수량은 0보다 작아질 수 없습니다.");
        }
        if (next > item.getTotalQuantity()) {
            throw new IllegalArgumentException("현재 수량은 총 수량을 초과할 수 없습니다.");
        }

        item.setCurrentQuantity(next);
        return toItemDto(rentalItemRepository.save(item));
    }

    public String uploadRentalItemImage(MultipartFile file) {
        try {
            return s3UploadService.saveFile(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    public List<RentalRecordResponseDto> getAllRecordsForAdmin() {
        return rentalRecordRepository.findAllByOrderByRentalDateDescCreatedAtDesc()
                .stream()
                .map(this::toRecordDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RentalRecordResponseDto createRecord(RentalRecordCreateRequestDto requestDto) {
        RentalItem item = rentalItemRepository.findById(requestDto.getRentalItemId())
                .orElseThrow(() -> new IllegalArgumentException("대여 품목을 찾을 수 없습니다. ID: " + requestDto.getRentalItemId()));

        if (Boolean.FALSE.equals(item.getActive())) {
            throw new IllegalArgumentException("비활성 품목은 대여할 수 없습니다.");
        }
        if (requestDto.getQuantity() > item.getCurrentQuantity()) {
            throw new IllegalArgumentException("현재 수량보다 많이 대여할 수 없습니다.");
        }

        item.setCurrentQuantity(item.getCurrentQuantity() - requestDto.getQuantity());

        RentalRecord record = RentalRecord.builder()
                .rentalItem(item)
                .quantity(requestDto.getQuantity())
                .managerName(requestDto.getManagerName())
                .rentalDate(requestDto.getRentalDate())
                .borrowerName(requestDto.getBorrowerName())
                .borrowerDepartment(requestDto.getBorrowerDepartment())
                .borrowerStudentId(requestDto.getBorrowerStudentId())
                .borrowerPhone(requestDto.getBorrowerPhone())
                .borrowerSignature(requestDto.getBorrowerSignature())
                .note(requestDto.getNote())
                .build();

        rentalItemRepository.save(item);
        return toRecordDto(rentalRecordRepository.save(record));
    }

    @Transactional
    public RentalRecordResponseDto markAsReturned(Long recordId, RentalRecordReturnRequestDto requestDto) {
        RentalRecord record = rentalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("대여 기록을 찾을 수 없습니다. ID: " + recordId));

        if (record.getReturnedAt() != null) {
            throw new IllegalArgumentException("이미 반납 처리된 기록입니다.");
        }

        RentalItem item = record.getRentalItem();
        int restoredQuantity = Math.min(item.getTotalQuantity(), item.getCurrentQuantity() + record.getQuantity());
        item.setCurrentQuantity(restoredQuantity);

        record.setReturnedAt(LocalDateTime.now());
        record.setManagerConfirmation(requestDto.getManagerConfirmation());

        rentalItemRepository.save(item);
        return toRecordDto(rentalRecordRepository.save(record));
    }

    private void validateItemQuantities(Integer current, Integer total) {
        if (current > total) {
            throw new IllegalArgumentException("현재 수량은 총 수량을 초과할 수 없습니다.");
        }
    }

    private RentalItemResponseDto toItemDto(RentalItem item) {
        return RentalItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .totalQuantity(item.getTotalQuantity())
                .currentQuantity(item.getCurrentQuantity())
                .imageUrl(item.getImageUrl())
                .note(item.getNote())
                .displayOrder(item.getDisplayOrder())
                .active(item.getActive())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private RentalRecordResponseDto toRecordDto(RentalRecord record) {
        return RentalRecordResponseDto.builder()
                .id(record.getId())
                .rentalItemId(record.getRentalItem().getId())
                .rentalItemName(record.getRentalItem().getName())
                .quantity(record.getQuantity())
                .managerName(record.getManagerName())
                .rentalDate(record.getRentalDate())
                .borrowerName(record.getBorrowerName())
                .borrowerDepartment(record.getBorrowerDepartment())
                .borrowerStudentId(record.getBorrowerStudentId())
                .borrowerPhone(record.getBorrowerPhone())
                .borrowerSignature(record.getBorrowerSignature())
                .returnedAt(record.getReturnedAt())
                .managerConfirmation(record.getManagerConfirmation())
                .note(record.getNote())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
