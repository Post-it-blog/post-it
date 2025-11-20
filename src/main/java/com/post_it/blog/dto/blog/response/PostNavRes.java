package com.post_it.blog.dto.blog.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostNavRes {
    private Long postId;
    private String title;
}