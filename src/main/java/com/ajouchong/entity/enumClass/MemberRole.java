package com.ajouchong.entity.enumClass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole{
    ADMIN("ROLE_ADMIN", "관리자"),
    STUDENT("ROLE_STUDENT", "학생"),
    COUNCIL("ROLE_COUNCIL", "학생회");

    private final String key;
    private final String title;
}
