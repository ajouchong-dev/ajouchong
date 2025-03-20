package com.ajouchong.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter  @Setter
@RequiredArgsConstructor
public class QnaPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qPostId;

    @NotNull
    private String qpAuthor;

    @NotNull
    private String qpTitle;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String qpContent;

    private boolean isReplied = false;

    private int qpUserLikeCnt = 0;
    private int qpHitCnt = 0;

    private LocalDateTime qpCreateTime;
    private LocalDateTime qpUpdateTime;

    @OneToOne(mappedBy = "qnaPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    @PrePersist
    protected void onCreate() {
        this.qpCreateTime = LocalDateTime.now();
        this.qpUpdateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.qpUpdateTime = LocalDateTime.now();
    }

    public void incrementHitCount() {
        this.qpHitCnt++;
    }

    public void incrementUserLikeCount() {
        this.qpUserLikeCnt++;
    }

}
