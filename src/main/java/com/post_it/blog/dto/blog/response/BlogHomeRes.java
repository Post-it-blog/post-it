package com.post_it.blog.dto.blog.response;

import com.post_it.blog.dto.common.PageInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BlogHomeRes {
    private BlogInfoRes blogInfo;
    private List<PostRes> postList;
    private List<CategoryRes> categoryList;
    private PageInfo pageInfo;
    private List<PostRes> recentPostList;
    private int totalPostCount;
}