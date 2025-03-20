package com.ajouchong.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class QnaPostRequestDto {
    private String qpTitle;
    private String qpContent;
}
