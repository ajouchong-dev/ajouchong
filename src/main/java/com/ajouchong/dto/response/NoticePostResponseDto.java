package com.ajouchong.dto.response;

import com.ajouchong.entity.NoticePost;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class NoticePostResponseDto {
    private Long nPost_id;
    private String npTitle;
    private String npContent;
    private int npUserLikeCnt;
    private int npHitCnt;
    private LocalDateTime npCreateTime;
    private LocalDateTime npUpdateTime;
    private List<String> imageUrls;     // 이미지 파일 URL
    private boolean likedByCurrentUser;

    public NoticePostResponseDto(NoticePost noticePost, boolean likedByCurrentUser) {
        this.nPost_id = noticePost.getNPostId();
        this.npTitle = noticePost.getNpTitle();
        this.npContent = noticePost.getNpContent();
        this.npUserLikeCnt = noticePost.getNpUserLikeCnt();
        this.npHitCnt = noticePost.getNpHitCnt();
        this.npCreateTime = noticePost.getNpCreateTime();
        this.npUpdateTime = noticePost.getNpUpdateTime();
        this.imageUrls = new ArrayList<>(noticePost.getImageUrls());
        this.likedByCurrentUser = likedByCurrentUser; // 사용자 좋아요 여부 설정
    }
}

