package com.post_it.blog.dto.main.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BlogListRes {
    private Long blogId;
    private String blogTitle;
    private String blogDesc;
    private String profileImg;
    private String memberNickname;
}