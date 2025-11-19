package com.post_it.blog.dto.blog.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlogInfoRes {
    private Long blogId;
    private String memberNickname;
    private String blogTitle;
    private String blogDesc;
    private String profileImg;
    private int totalPostCount;
}
