package com.post_it.blog.dto.comment.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CommentRes {
    private Long commentId;
    private Long postId;
    private Long memberId;
    private String memberNickname;
    private String content;
    private LocalDateTime createdAt;
    private Long parentCommentId;
    private String profileImg;
    private Long blogId; // [추가] 댓글 작성자의 블로그 ID (없으면 null)

    @Builder.Default
    private List<CommentRes> replies = new ArrayList<>();
}