package com.post_it.blog.dto.blog.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BlogProfileRes {
    private Long blogId;
    private String blogTitle;
    private String blogDesc;
    private String profileImg;

    private List<CategoryRes> categoryList;
}