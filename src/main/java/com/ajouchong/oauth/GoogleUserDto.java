package com.ajouchong.oauth;

import lombok.Data;

@Data
public class GoogleUserDto {
    private String email;
    private String name;
    private String role;
}
