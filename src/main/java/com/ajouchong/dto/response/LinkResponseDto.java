package com.ajouchong.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LinkResponseDto {
    private Long id;
    private String title;
    private String link;
    private Boolean active;
    private Boolean showLink;
    private LocalDateTime createdAt;
}