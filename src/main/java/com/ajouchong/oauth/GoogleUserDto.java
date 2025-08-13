package com.ajouchong.oauth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserDto {
    private String email;
    private String name;
    private String picture;
    private String locale;
    private String sub;
}
