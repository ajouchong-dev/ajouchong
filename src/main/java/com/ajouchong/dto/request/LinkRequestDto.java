package com.ajouchong.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class LinkRequestDto {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotBlank(message = "링크는 필수입니다.")
    private String link;
}
