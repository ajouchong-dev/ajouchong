package com.ajouchong.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Partnership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long psPostId;

    private String psTitle;

    @Column(columnDefinition = "TEXT")
    private String psContent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "partner_images", joinColumns = @JoinColumn(name = "psPostId"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>(); // S3에 저장된 이미지 URL 리스트

    private int psUserLikeCnt = 0;
    private int psHitCnt = 0;

    private LocalDateTime psCreateTime;
    private LocalDateTime psUpdateTime;

    @PrePersist
    protected void onCreate() {
        this.psCreateTime = LocalDateTime.now();
        this.psUpdateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.psUpdateTime = LocalDateTime.now();
    }
}
