package com.post_it.blog.repository;

import com.post_it.blog.dto.blog.request.BlogInfoUpdateReq;
import com.post_it.blog.dto.blog.request.PostWriteReq;
import com.post_it.blog.dto.blog.response.*;
import com.post_it.blog.dto.common.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlogRepository {

    private final JdbcTemplate jdbcTemplate;

    public BlogInfoRes findBlogInfo(Long blogId) {
        try {
            String sql = "SELECT B.BLOG_ID, M.NICKNAME, B.BLOG_TITLE, B.BLOG_DESC, B.PROFILE_IMG " +
                    "FROM BLOG B " +
                    "JOIN MEMBER M ON B.MEMBER_ID = M.MEMBER_ID " +
                    "WHERE B.BLOG_ID = ?";
            RowMapper<BlogInfoRes> rowMapper = (rs, rowNum) -> BlogInfoRes.builder()
                    .blogId(rs.getLong("BLOG_ID"))
                    .memberNickname(rs.getString("NICKNAME"))
                    .blogTitle(rs.getString("BLOG_TITLE"))
                    .blogDesc(rs.getString("BLOG_DESC"))
                    .profileImg(rs.getString("PROFILE_IMG"))
                    .build();
            return jdbcTemplate.queryForObject(sql, rowMapper, blogId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // [수정] 게시글 상세 조회
    public PostDetailRes findPostDetail(Long postId) {
        try {
            String sql = "SELECT P.POST_ID, P.TITLE, P.CONTENT, P.LIKE_COUNT, P.CREATED_AT, " +
                    "       C.CATEGORY_NAME, P.CATEGORY_ID, M.NICKNAME, M.MEMBER_ID " +
                    "FROM POST P " +
                    "JOIN CATEGORY C ON P.CATEGORY_ID = C.CATEGORY_ID " +
                    "JOIN BLOG B ON P.BLOG_ID = B.BLOG_ID " +
                    "JOIN MEMBER M ON B.MEMBER_ID = M.MEMBER_ID " +
                    "WHERE P.POST_ID = ?";

            RowMapper<PostDetailRes> mapper = (rs, rowNum) -> PostDetailRes.builder()
                    .postId(rs.getLong("POST_ID"))
                    .memberId(rs.getLong("MEMBER_ID"))
                    .title(rs.getString("TITLE"))
                    .content(rs.getString("CONTENT"))
                    .likeCount(rs.getInt("LIKE_COUNT"))
                    .createdAt(rs.getTimestamp("CREATED_AT").toLocalDateTime())
                    .categoryName(rs.getString("CATEGORY_NAME"))
                    .categoryId(rs.getLong("CATEGORY_ID"))
                    .writerNickname(rs.getString("NICKNAME"))
                    .build();

            return jdbcTemplate.queryForObject(sql, mapper, postId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // [추가] 좋아요 상태 확인
    public boolean isLiked(Long postId, Long memberId) {
        String sql = "SELECT COUNT(*) FROM POST_LIKE WHERE POST_ID = ? AND MEMBER_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId, memberId);
        return count != null && count > 0;
    }

    // [추가] 좋아요 추가
    public void addLike(Long postId, Long memberId) {
        String sql = "INSERT INTO POST_LIKE (POST_ID, MEMBER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, postId, memberId);
        // 게시글 카운트 증가
        jdbcTemplate.update("UPDATE POST SET LIKE_COUNT = LIKE_COUNT + 1 WHERE POST_ID = ?", postId);
    }

    // [추가] 좋아요 취소
    public void removeLike(Long postId, Long memberId) {
        String sql = "DELETE FROM POST_LIKE WHERE POST_ID = ? AND MEMBER_ID = ?";
        jdbcTemplate.update(sql, postId, memberId);
        // 게시글 카운트 감소
        jdbcTemplate.update("UPDATE POST SET LIKE_COUNT = LIKE_COUNT - 1 WHERE POST_ID = ?", postId);
    }

    // [추가] 현재 좋아요 수 조회 (반환용)
    public int getLikeCount(Long postId) {
        String sql = "SELECT LIKE_COUNT FROM POST WHERE POST_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, postId);
    }

    // [추가] 게시글 수정
    public void updatePost(Long postId, PostWriteReq req) {
        String sql = "UPDATE POST SET CATEGORY_ID = ?, TITLE = ?, CONTENT = ?, UPDATED_AT = SYSDATE WHERE POST_ID = ?";
        jdbcTemplate.update(sql, req.getCategoryId(), req.getTitle(), req.getContent(), postId);
    }

    // [추가] 게시글 삭제
    public void deletePost(Long postId) {
        String sql = "DELETE FROM POST WHERE POST_ID = ?";
        jdbcTemplate.update(sql, postId);
    }

    // --- [추가] 이전 글 조회 (같은 블로그 내에서 ID가 작은 것 중 가장 큰 것) ---
    public PostNavRes findPrevPost(Long blogId, Long postId) {
        try {
            String sql = "SELECT * FROM ( " +
                    "  SELECT POST_ID, TITLE FROM POST " +
                    "  WHERE BLOG_ID = ? AND POST_ID < ? " +
                    "  ORDER BY POST_ID DESC " +
                    ") WHERE ROWNUM = 1";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new PostNavRes(rs.getLong("POST_ID"), rs.getString("TITLE")), blogId, postId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // --- [추가] 다음 글 조회 (같은 블로그 내에서 ID가 큰 것 중 가장 작은 것) ---
    public PostNavRes findNextPost(Long blogId, Long postId) {
        try {
            String sql = "SELECT * FROM ( " +
                    "  SELECT POST_ID, TITLE FROM POST " +
                    "  WHERE BLOG_ID = ? AND POST_ID > ? " +
                    "  ORDER BY POST_ID ASC " +
                    ") WHERE ROWNUM = 1";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new PostNavRes(rs.getLong("POST_ID"), rs.getString("TITLE")), blogId, postId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int countTotalPosts(Long blogId) {
        String sql = "SELECT COUNT(*) FROM POST WHERE BLOG_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, blogId);
    }

    public int countTotalPostsByCategory(Long blogId, Long categoryId) {
        String sql = "SELECT COUNT(*) FROM POST WHERE BLOG_ID = ? AND CATEGORY_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, blogId, categoryId);
    }

    public int countTotalPostsBySearch(Long blogId, String query) {
        String searchPattern = "%" + query + "%";

        String sql = "SELECT COUNT(*) FROM POST WHERE BLOG_ID = ? AND (TITLE LIKE ? OR REGEXP_REPLACE(CONTENT, '<[^>]*>', '') LIKE ?)";
        return jdbcTemplate.queryForObject(sql, Integer.class, blogId, searchPattern, searchPattern);
    }

    public List<PostRes> findPostsWithPaging(Long blogId, PageInfo pageInfo) {
        String sql = "SELECT RNUM, POST_ID, TITLE, CONTENT, CATEGORY_NAME, CREATED_AT, MAIN_IMAGE_URL " +
                "FROM ( " +
                "  SELECT " +
                "    ROWNUM AS RNUM, " +
                "    T.POST_ID, T.TITLE, T.CONTENT, T.CATEGORY_NAME, T.CREATED_AT, T.MAIN_IMAGE_URL " +
                "  FROM ( " +
                "    SELECT " +
                "      P.POST_ID, P.TITLE, P.CONTENT, C.CATEGORY_NAME, P.CREATED_AT, " +
                "      (SELECT IMG_URL FROM POST_IMAGE PI WHERE PI.POST_ID = P.POST_ID AND ROWNUM = 1) AS MAIN_IMAGE_URL " +
                "    FROM POST P " +
                "    JOIN CATEGORY C ON P.CATEGORY_ID = C.CATEGORY_ID " +
                "    WHERE P.BLOG_ID = ? " +
                "    ORDER BY P.CREATED_AT DESC " +
                "  ) T " +
                "  WHERE ROWNUM <= ? " +
                ") " +
                "WHERE RNUM >= ?";
        RowMapper<PostRes> rowMapper = getPostResRowMapper();
        return jdbcTemplate.query(sql, rowMapper, blogId, pageInfo.getEndRow(), pageInfo.getStartRow());
    }

    public List<PostRes> findPostsByCategoryWithPaging(Long blogId, Long categoryId, PageInfo pageInfo) {
        String sql = "SELECT RNUM, POST_ID, TITLE, CONTENT, CATEGORY_NAME, CREATED_AT, MAIN_IMAGE_URL " +
                "FROM ( " +
                "  SELECT " +
                "    ROWNUM AS RNUM, " +
                "    T.POST_ID, T.TITLE, T.CONTENT, T.CATEGORY_NAME, T.CREATED_AT, T.MAIN_IMAGE_URL " +
                "  FROM ( " +
                "    SELECT " +
                "      P.POST_ID, P.TITLE, P.CONTENT, C.CATEGORY_NAME, P.CREATED_AT, " +
                "      (SELECT IMG_URL FROM POST_IMAGE PI WHERE PI.POST_ID = P.POST_ID AND ROWNUM = 1) AS MAIN_IMAGE_URL " +
                "    FROM POST P " +
                "    JOIN CATEGORY C ON P.CATEGORY_ID = C.CATEGORY_ID " +
                "    WHERE P.BLOG_ID = ? AND P.CATEGORY_ID = ? " +
                "    ORDER BY P.CREATED_AT DESC " +
                "  ) T " +
                "  WHERE ROWNUM <= ? " +
                ") " +
                "WHERE RNUM >= ?";
        RowMapper<PostRes> rowMapper = getPostResRowMapper();
        return jdbcTemplate.query(sql, rowMapper, blogId, categoryId, pageInfo.getEndRow(), pageInfo.getStartRow());
    }

    public List<PostRes> findPostsBySearchWithPaging(Long blogId, String query, PageInfo pageInfo) {
        String searchPattern = "%" + query + "%";

        String sql = "SELECT RNUM, POST_ID, TITLE, CONTENT, CATEGORY_NAME, CREATED_AT, MAIN_IMAGE_URL " +
                "FROM ( " +
                "  SELECT " +
                "    ROWNUM AS RNUM, " +
                "    T.POST_ID, T.TITLE, T.CONTENT, T.CATEGORY_NAME, T.CREATED_AT, T.MAIN_IMAGE_URL " +
                "  FROM ( " +
                "    SELECT " +
                "      P.POST_ID, P.TITLE, P.CONTENT, C.CATEGORY_NAME, P.CREATED_AT, " +
                "      (SELECT IMG_URL FROM POST_IMAGE PI WHERE PI.POST_ID = P.POST_ID AND ROWNUM = 1) AS MAIN_IMAGE_URL " +
                "    FROM POST P " +
                "    JOIN CATEGORY C ON P.CATEGORY_ID = C.CATEGORY_ID " +
                "    WHERE P.BLOG_ID = ? AND (P.TITLE LIKE ? OR REGEXP_REPLACE(P.CONTENT, '<[^>]*>', '') LIKE ?) " +
                "    ORDER BY P.CREATED_AT DESC " +
                "  ) T " +
                "  WHERE ROWNUM <= ? " +
                ") " +
                "WHERE RNUM >= ?";

        RowMapper<PostRes> rowMapper = getPostResRowMapper();
        return jdbcTemplate.query(sql, rowMapper, blogId, searchPattern, searchPattern, pageInfo.getEndRow(), pageInfo.getStartRow());
    }

    public List<PostRes> findRecentPosts(Long blogId) {
        String sql = "SELECT POST_ID, TITLE, CREATED_AT " +
                "FROM ( " +
                "    SELECT POST_ID, TITLE, CREATED_AT " +
                "    FROM POST " +
                "    WHERE BLOG_ID = ? " +
                "    ORDER BY CREATED_AT DESC " +
                ") " +
                "WHERE ROWNUM <= 5";

        RowMapper<PostRes> rowMapper = (rs, rowNum) -> PostRes.builder()
                .postId(rs.getLong("POST_ID"))
                .title(rs.getString("TITLE"))
                .createdAt(rs.getDate("CREATED_AT"))
                .build();

        return jdbcTemplate.query(sql, rowMapper, blogId);
    }

    public List<CategoryRes> findCategoriesWithCount(Long blogId) {
        String sql = "SELECT " +
                "  C.CATEGORY_ID, C.CATEGORY_NAME, COUNT(P.POST_ID) AS POST_COUNT " +
                "FROM CATEGORY C " +
                "LEFT JOIN POST P ON C.CATEGORY_ID = P.CATEGORY_ID " +
                "WHERE C.BLOG_ID = ? " +
                "GROUP BY C.CATEGORY_ID, C.CATEGORY_NAME " +
                "ORDER BY C.CATEGORY_ID";

        RowMapper<CategoryRes> rowMapper = (rs, rowNum) -> CategoryRes.builder()
                .categoryId(rs.getLong("CATEGORY_ID"))
                .categoryName(rs.getString("CATEGORY_NAME"))
                .postCount(rs.getInt("POST_COUNT"))
                .build();

        return jdbcTemplate.query(sql, rowMapper, blogId);
    }

    public boolean isMyBlog(Long blogId, Long memberId) {
        String sql = "select count(*) from blog where blog_id = ? and member_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, blogId, memberId);
        return count != null && count > 0;
    }

    public Long savePost(Long blogId, PostWriteReq req) {
        String sql = "INSERT INTO POST (BLOG_ID, CATEGORY_ID, TITLE, CONTENT) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, blogId, req.getCategoryId(), req.getTitle(), req.getContent());
        return jdbcTemplate.queryForObject("SELECT POST_SEQ.CURRVAL FROM DUAL", Long.class);
    }

    public void savePostImage(Long postId, String url, String fileName) {
        String sql = "INSERT INTO POST_IMAGE (POST_ID, IMG_URL, FILE_NAME) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, postId, url, fileName);
    }

    public void updateBlogInfo(Long blogId, BlogInfoUpdateReq req) {
        String sql = "UPDATE BLOG SET BLOG_TITLE = ?, BLOG_DESC = ? WHERE BLOG_ID = ?";
        jdbcTemplate.update(sql, req.getBlogTitle(), req.getBlogDesc(), blogId);
    }

    // [추가] 블로그 프로필 이미지 수정
    public void updateBlogProfileImg(Long blogId, String profileImg) {
        String sql = "UPDATE BLOG SET PROFILE_IMG = ? WHERE BLOG_ID = ?";
        jdbcTemplate.update(sql, profileImg, blogId);
    }

    // [추가] 카테고리 추가
    public void addCategory(Long blogId, String categoryName) {
        String sql = "INSERT INTO CATEGORY (BLOG_ID, CATEGORY_NAME) VALUES (?, ?)";
        jdbcTemplate.update(sql, blogId, categoryName);
    }

    // [추가] 카테고리 수정
    public void updateCategory(Long categoryId, String categoryName) {
        String sql = "UPDATE CATEGORY SET CATEGORY_NAME = ? WHERE CATEGORY_ID = ?";
        jdbcTemplate.update(sql, categoryName, categoryId);
    }

    // [추가] 카테고리 삭제
    public void deleteCategory(Long categoryId) {
        String sql = "DELETE FROM CATEGORY WHERE CATEGORY_ID = ?";
        jdbcTemplate.update(sql, categoryId);
    }

    // [추가] 특정 블로그의 카테고리 개수 조회
    public int countCategories(Long blogId) {
        String sql = "SELECT COUNT(*) FROM CATEGORY WHERE BLOG_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, blogId);
    }

    // [추가] 카테고리 ID로 블로그 ID 조회 (삭제 시 검증용)
    public Long findBlogIdByCategoryId(Long categoryId) {
        try {
            String sql = "SELECT BLOG_ID FROM CATEGORY WHERE CATEGORY_ID = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, categoryId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private RowMapper<PostRes> getPostResRowMapper() {
        return (rs, rowNum) -> PostRes.builder()
                .postId(rs.getLong("POST_ID"))
                .title(rs.getString("TITLE"))
                .content(rs.getString("CONTENT"))
                .categoryName(rs.getString("CATEGORY_NAME"))
                .createdAt(rs.getDate("CREATED_AT"))
                .mainImageUrl(rs.getString("MAIN_IMAGE_URL"))
                .build();
    }
}