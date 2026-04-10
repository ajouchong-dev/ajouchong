package com.ajouchong.controller.user;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.dto.response.LinkResponseDto;
import com.ajouchong.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/link")
@RequiredArgsConstructor
public class LinkController {
    private final LinkService linkService;
    
    @GetMapping
    public ApiResponse<List<LinkResponseDto>> getAllLinks() {
        List<LinkResponseDto> links = linkService.getActiveLinks();
        
        return new ApiResponse<>(1, "링크 목록을 조회했습니다.", links);
    }
    
    @DeleteMapping("/{id}/delete")
    public ApiResponse<Void> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        
        return new ApiResponse<>(1, "링크가 삭제되었습니다.", null);
    }
}