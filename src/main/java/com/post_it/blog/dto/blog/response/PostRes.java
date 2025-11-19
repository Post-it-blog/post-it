package com.post_it.blog.dto.blog.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class PostRes {
    private Long postId;
    private String title;
    private String content;
    private String contentPreview;
    private String categoryName;
    private Date createdAt;
    private String mainImageUrl;
}
