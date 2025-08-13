package com.ajouchong.oauth;

import com.ajouchong.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthResponseDto {
    private String jwtToken;
    private String refreshToken;
    private Member member;
    private Long expiresIn;
    
    public OAuthResponseDto(String jwtToken, Member member) {
        this.jwtToken = jwtToken;
        this.member = member;
    }
}
