package com.post_it.blog.service;

import com.post_it.blog.dto.common.PageInfo;
import com.post_it.blog.dto.main.response.BestPostRes;
import com.post_it.blog.dto.main.response.BlogListRes;

import java.util.List;

public interface MainService {

    List<BestPostRes> getBestPosts();

    List<BlogListRes> getBlogList(int currentPage, String searchCategory, String searchWord);

    PageInfo getPageInfo(int currentPage, String searchCategory, String searchWord);

    Long getMyBlogId(Long memberId);
}