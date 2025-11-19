package com.post_it.blog.dto.blog.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryRes {
    private Long categoryId;
    private String categoryName;
    private int postCount;
}