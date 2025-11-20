package com.post_it.blog.dto.main.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BestPostRes {
    private Long postId;
    private Long blogId;
    private String title;
    private String memberNickname;
    private String mainImageUrl;
}