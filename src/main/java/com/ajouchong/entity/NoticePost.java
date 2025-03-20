package com.ajouchong.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@RequiredArgsConstructor
@Getter @Setter
public class NoticePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nPostId;

    private String npTitle;

    @Column(columnDefinition = "TEXT")
    private String npContent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "noticePost_images", joinColumns = @JoinColumn(name = "nPostId"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>(); // S3에 저장된 이미지 URL 리스트

    private int npUserLikeCnt = 0;
    private int npHitCnt = 0;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member author;

    private LocalDateTime npCreateTime;
    private LocalDateTime npUpdateTime;

    @PrePersist
    protected void onCreate() {
        this.npCreateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        this.npUpdateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @PreUpdate
    protected void onUpdate() {
        this.npUpdateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @Builder
    public NoticePost(String npTitle, String npContent, Member author, List<String> imageUrls) {
        this.npTitle = npTitle;
        this.npContent = npContent;
        this.author = author;
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        this.npHitCnt = 0;
        this.npUserLikeCnt = 0;
    }
}
