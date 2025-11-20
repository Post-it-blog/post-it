package com.post_it.blog.dto.blog.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailRes {
    private Long postId;
    private Long memberId;
    private String title;
    private String content;
    private String categoryName;
    private Long categoryId;
    private String writerNickname;
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean liked; // [추가] 현재 로그인한 사용자가 좋아요를 눌렀는지 여부

    private PostNavRes prevPost;
    private PostNavRes nextPost;
}