package com.post_it.blog.dto.member.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class SignUpReq {
    private String userId;
    private String password;
    private String name;
    private String nickname;
    private String email;
}
