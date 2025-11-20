package com.post_it.blog.repository;

import com.post_it.blog.dto.comment.request.CommentWriteReq;
import com.post_it.blog.dto.comment.response.CommentRes;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<CommentRes> findAllByPostId(Long postId) {
        String sql = "SELECT C.COMMENT_ID, C.POST_ID, C.MEMBER_ID, M.NICKNAME, " +
                "       C.PARENT_COMMENT_ID, C.CONTENT, C.CREATED_AT, " +
                "       B.PROFILE_IMG, B.BLOG_ID " + // BLOG_ID 추가
                "FROM COMMENTS C " +
                "LEFT JOIN MEMBER M ON C.MEMBER_ID = M.MEMBER_ID " +
                "LEFT JOIN BLOG B ON M.MEMBER_ID = B.MEMBER_ID " +
                "WHERE C.POST_ID = ? " +
                "ORDER BY C.CREATED_AT ASC";

        RowMapper<CommentRes> rowMapper = (rs, rowNum) -> CommentRes.builder()
                .commentId(rs.getLong("COMMENT_ID"))
                .postId(rs.getLong("POST_ID"))
                .memberId(rs.getObject("MEMBER_ID", Long.class))
                .memberNickname(rs.getString("NICKNAME"))
                .parentCommentId(rs.getObject("PARENT_COMMENT_ID", Long.class))
                .content(rs.getString("CONTENT"))
                .createdAt(rs.getTimestamp("CREATED_AT").toLocalDateTime())
                .profileImg(rs.getString("PROFILE_IMG"))
                .blogId(rs.getObject("BLOG_ID", Long.class)) // 블로그 ID 매핑
                .build();

        return jdbcTemplate.query(sql, rowMapper, postId);
    }

    // [추가] 댓글 단건 조회 (권한 확인용)
    public CommentRes findById(Long commentId) {
        String sql = "SELECT MEMBER_ID FROM COMMENTS WHERE COMMENT_ID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    CommentRes.builder().memberId(rs.getLong("MEMBER_ID")).build(), commentId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // [추가] 댓글 수정
    public void update(Long commentId, String content) {
        String sql = "UPDATE COMMENTS SET CONTENT = ? WHERE COMMENT_ID = ?";
        jdbcTemplate.update(sql, content, commentId);
    }

    // [추가] 댓글 삭제
    public void delete(Long commentId) {
        String sql = "DELETE FROM COMMENTS WHERE COMMENT_ID = ?";
        jdbcTemplate.update(sql, commentId);
    }

    public int countByPostId(Long postId) {
        String sql = "SELECT COUNT(*) FROM COMMENTS WHERE POST_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, postId);
    }

    public void save(CommentWriteReq req, Long memberId) {
        String sql = "INSERT INTO COMMENTS (POST_ID, MEMBER_ID, PARENT_COMMENT_ID, CONTENT) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, req.getPostId(), memberId, req.getParentCommentId(), req.getContent());
    }
}