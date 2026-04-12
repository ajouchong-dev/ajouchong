package com.ajouchong.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticePostUpdateFormDto {
    private String title;
    private String content;
    private List<MultipartFile> imageFiles;
}
