package com.post_it.blog.dto.member.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MemberRes {
    private Long memberId;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;

    public MemberRes(long memberId, String nickname, String role, LocalDateTime createdAt) {
    }
}
