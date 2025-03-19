package com.ajouchong.dto.response;

import com.ajouchong.entity.Partnership;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnershipResponseDto {
    private Long psPostId;
    private String psTitle;
    private String psContent;
    private int psUserLikeCnt;
    private int psHitCnt;
    private LocalDateTime psCreateTime;
    private LocalDateTime psUpdateTime;
    private List<String> imageUrls;
    private boolean likedByCurrentMember;

    public PartnershipResponseDto(Partnership partnership, boolean likedByCurrentMember) {
        this.psPostId = partnership.getPsPostId();
        this.psTitle = partnership.getPsTitle();
        this.psContent = partnership.getPsContent();
        this.psUserLikeCnt = partnership.getPsUserLikeCnt();
        this.psHitCnt = partnership.getPsHitCnt();
        this.psCreateTime = partnership.getPsCreateTime();
        this.psUpdateTime = partnership.getPsUpdateTime();
        this.imageUrls = new ArrayList<>(partnership.getImageUrls());
        this.likedByCurrentMember = likedByCurrentMember;
    }
}
