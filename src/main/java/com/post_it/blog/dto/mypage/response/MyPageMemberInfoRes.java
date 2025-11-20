package com.post_it.blog.dto.mypage.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MyPageMemberInfoRes {
    private String name;
    private String nickname;
    private String userId;
    private String email;
    private String profileImg;
    private Long blogId;
}
