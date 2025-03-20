package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.PartnershipFormRequestDto;
import com.ajouchong.dto.request.PartnershipRequestDto;
import com.ajouchong.dto.response.PartnershipResponseDto;
import com.ajouchong.service.PartnershipService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/admin/partnership")
public class PartnershipAdminController {
    private final PartnershipService partnershipService;

    public PartnershipAdminController(PartnershipService partnershipService) {
        this.partnershipService = partnershipService;
    }

    @PostMapping
    public ApiResponse<PartnershipResponseDto> uploadPartnership(@ModelAttribute PartnershipFormRequestDto requestDto) throws IOException {
        PartnershipRequestDto partnershipRequestDto = PartnershipRequestDto.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imageFiles(requestDto.getImageFiles())
                .build();

        PartnershipResponseDto savedPartnership = partnershipService.savePartnership(partnershipRequestDto);

        return new ApiResponse<>(1, "제휴 백과 업로드 성공", savedPartnership);
    }

//    @PutMapping("/{id}")
//    public ApiResponse<PartnershipResponseDto> changePartnership(@PathVariable Long id, @RequestBody PartnershipRequestDto requestDto) {
//        PartnershipResponseDto updatedPartner = partnershipService.changePartnership(id, requestDto);
//        return new ApiResponse<>(1, id + "번 게시글 수정 성공", updatedPartner);
//    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePartnership(@PathVariable Long id) {
        partnershipService.deletePartnership(id);
        return new ApiResponse<>(1, id + "번 게시글 삭제 성공", null);
    }

}
