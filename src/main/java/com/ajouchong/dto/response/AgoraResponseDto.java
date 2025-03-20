package com.ajouchong.dto.response;

import com.ajouchong.entity.Agora;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AgoraResponseDto {
    private Long aPostId;
    private String apTitle;
    private String apContent;
    private String author;
    private boolean isApprove = false;
    private int apUserLikeCount;
    private int apHitCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private AnswerDto answer;
    private boolean likedByCurrentMember;

    @Getter
    @Setter
    public static class AnswerDto {
        private Long answerId;
        private String content;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    public AgoraResponseDto(Agora post, boolean likedByCurrentMember) {
        this.aPostId = post.getAPostId();
        this.apTitle = post.getApTitle();
        this.author = post.getAuthor();
        this.apContent = post.getApContent();
        this.isApprove = post.isApprove();
        this.apUserLikeCount = post.getApUserLikeCount();
        this.apHitCount = post.getApHitCount();
        this.createTime = post.getCreateTime();
        this.updateTime = post.getUpdateTime();
        this.likedByCurrentMember = likedByCurrentMember;

    }
}
