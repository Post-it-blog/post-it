package com.post_it.blog.dto.comment.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentWriteReq {
    private Long postId;
    private Long parentCommentId;
    private String content;
}