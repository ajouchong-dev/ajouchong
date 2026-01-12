package com.ajouchong.entity;

import com.ajouchong.entity.enumClass.RuleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RulePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rPostId;

    private String rpTitle;
    @Column(columnDefinition = "TEXT")
    private String rpContent;
    @Column(columnDefinition = "TEXT")
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    private RuleType type; // 회칙 or 세칙 구분

    private int rpUserLikeCnt = 0;
    private int rpHitCnt = 0;

    private LocalDateTime rpCreateTime;
    private LocalDateTime rpUpdateTime;

    @PrePersist
    protected void onCreate() {
        this.rpCreateTime = LocalDateTime.now();
        this.rpUpdateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.rpUpdateTime = LocalDateTime.now();
    }

    public void incrementHitCount() {
        this.rpHitCnt++;
    }

    // 좋아요 수 증가
    public void incrementUserLikeCount() {
        this.rpUserLikeCnt++;
    }
}

