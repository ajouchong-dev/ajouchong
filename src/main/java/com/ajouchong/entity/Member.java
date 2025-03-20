package com.ajouchong.entity;

import com.ajouchong.entity.enumClass.MemberRole;
import com.ajouchong.oauth.GoogleUserDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member(GoogleUserDto googleUser) {
        this.email = googleUser.getEmail();
        this.name = googleUser.getName();
        this.role = (googleUser.getRole() != null) ?
                MemberRole.valueOf(googleUser.getRole()) : MemberRole.STUDENT;
    }

}
