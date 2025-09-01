package com.ajouchong.dto.response;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class LinkResponseDto {
    private Long id;
    private String title;
    private String link;
    private LocalDateTime createdAt;
}
