package com.ajouchong.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter  @Setter
public class Agora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aPostId;

    @NotNull
    private String author;

    @NotNull
    private String apTitle;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String apContent;

    private boolean approve = false;

    private int apUserLikeCount = 0;
    private int apHitCount = 0;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    public void incrementHitCount() {
        this.apHitCount++;
    }

    public void incrementUserLikeCount() {
        this.apUserLikeCount++;
    }

    public void approvePost() {
        this.approve = true;
    }

}

