package com.post_it.blog.service;

import com.post_it.blog.dto.blog.request.PostWriteReq;
import com.post_it.blog.dto.blog.response.BlogHomeRes;
import com.post_it.blog.dto.blog.response.CategoryRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BlogService {
    BlogHomeRes getBlogHomeData(Long blogId, int currentPage);

    BlogHomeRes getBlogCategoryData(Long blogId, Long categoryId, int currentPage);

    BlogHomeRes getBlogSearchData(Long blogId, String query, int currentPage);

    boolean isMyBlog(Long blogId, Long memberId);

    List<CategoryRes> getCategoryList(Long blogId);

    String uploadImage(MultipartFile file);

    void writePost(Long blogId, PostWriteReq postWriteReq);
}