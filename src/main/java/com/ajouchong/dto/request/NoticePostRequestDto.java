package com.ajouchong.dto.request;

import com.ajouchong.entity.Member;
import com.ajouchong.entity.NoticePost;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class NoticePostRequestDto {
    private Member author;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
    private List<MultipartFile> imageFiles = new ArrayList<>(); // 이미지 파일 리스트

    @Builder
    public NoticePostRequestDto(Member author, String title, String content, List<MultipartFile> imageFiles) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.imageFiles = imageFiles;
    }

    public NoticePost createNoticePost(List<String> imageUrls) {
        NoticePost noticePost = new NoticePost();
        noticePost.setAuthor(this.author);
        noticePost.setNpTitle(this.title);
        noticePost.setNpContent(this.content);
        noticePost.setImageUrls(imageUrls); // 이미지 URL 리스트 설정

        return noticePost;
    }
}
