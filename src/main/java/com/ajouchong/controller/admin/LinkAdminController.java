package com.ajouchong.controller.admin;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.request.LinkRequestDto;
import com.ajouchong.dto.response.LinkResponseDto;
import com.ajouchong.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/link")
@RequiredArgsConstructor
public class LinkAdminController {
    
    private final LinkService linkService;
    
    @PostMapping("/upload")
    public ApiResponse<LinkResponseDto> uploadLink(@Valid @RequestBody LinkRequestDto requestDto) {
        LinkResponseDto response = linkService.uploadLink(requestDto);
        
        return new ApiResponse<>(1, "링크가 성공적으로 업로드되었습니다.", response);
    }
    
    @GetMapping
    public ApiResponse<List<LinkResponseDto>> getAllLinks() {
        List<LinkResponseDto> links = linkService.getAllLinks();
        
        return new ApiResponse<>(1, "링크 목록을 조회했습니다.", links);
    }
    
    @DeleteMapping("/{id}/delete")
    public ApiResponse<Void> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        
        return new ApiResponse<>(1, "링크가 삭제되었습니다.", null);
    }
}
