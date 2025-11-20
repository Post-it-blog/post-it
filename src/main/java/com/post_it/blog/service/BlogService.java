package com.post_it.blog.service;

import com.post_it.blog.dto.blog.request.PostWriteReq;
import com.post_it.blog.dto.blog.response.BlogHomeRes;
import com.post_it.blog.dto.blog.response.CategoryRes;
import com.post_it.blog.dto.blog.response.PostDetailRes;
import com.post_it.blog.dto.comment.request.CommentUpdateReq;
import com.post_it.blog.dto.comment.request.CommentWriteReq;
import com.post_it.blog.dto.comment.response.CommentRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BlogService {
    BlogHomeRes getBlogHomeData(Long blogId, int currentPage);

    BlogHomeRes getBlogCategoryData(Long blogId, Long categoryId, int currentPage);

    BlogHomeRes getBlogSearchData(Long blogId, String query, int currentPage);

    PostDetailRes getPostDetail(Long blogId, Long postId, Long memberId);

    void editPost(Long postId, PostWriteReq req, Long memberId);

    void deletePost(Long postId, Long memberId);

    boolean toggleLike(Long postId, Long memberId);

    int getLikeCount(Long postId);

    List<CommentRes> getCommentList(Long postId);

    int getCommentCount(Long postId);

    void writeComment(CommentWriteReq req, Long memberId);

    void updateComment(CommentUpdateReq req, Long memberId);

    void deleteComment(Long commentId, Long memberId);

    boolean isMyBlog(Long blogId, Long memberId);

    List<CategoryRes> getCategoryList(Long blogId);

    String uploadImage(MultipartFile file);

    void writePost(Long blogId, PostWriteReq postWriteReq);
}