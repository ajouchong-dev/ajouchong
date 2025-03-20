package com.ajouchong.oauth;

import com.ajouchong.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthResponseDto {
    private String jwtToken;
    private Member member;
}
