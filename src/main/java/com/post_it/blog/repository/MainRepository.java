package com.post_it.blog.repository;

import com.post_it.blog.dto.common.PageInfo;
import com.post_it.blog.dto.main.response.BestPostRes;
import com.post_it.blog.dto.main.response.BlogListRes;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MainRepository {

    private final JdbcTemplate jdbcTemplate;

    // [1] 베스트 게시글 3개 조회 (좋아요 순)
    public List<BestPostRes> findBestPosts() {
        String sql = "SELECT * FROM ( " +
                "    SELECT P.POST_ID, P.BLOG_ID, P.TITLE, M.NICKNAME, P.LIKE_COUNT, " +
                "           (SELECT IMG_URL FROM POST_IMAGE PI WHERE PI.POST_ID = P.POST_ID AND ROWNUM = 1) AS MAIN_IMAGE_URL " +
                "    FROM POST P " +
                "    JOIN BLOG B ON P.BLOG_ID = B.BLOG_ID " +
                "    JOIN MEMBER M ON B.MEMBER_ID = M.MEMBER_ID " +
                "    ORDER BY P.LIKE_COUNT DESC, P.CREATED_AT DESC " +
                ") WHERE ROWNUM <= 3";

        RowMapper<BestPostRes> mapper = (rs, rowNum) -> BestPostRes.builder()
                .postId(rs.getLong("POST_ID"))
                .blogId(rs.getLong("BLOG_ID"))
                .title(rs.getString("TITLE"))
                .memberNickname(rs.getString("NICKNAME"))
                .mainImageUrl(rs.getString("MAIN_IMAGE_URL"))
                .build();

        return jdbcTemplate.query(sql, mapper);
    }

    // [2] 블로그 목록 검색 개수 조회
    public int countBlogList(String searchCategory, String searchWord) {
        String sql = "SELECT COUNT(*) FROM BLOG B JOIN MEMBER M ON B.MEMBER_ID = M.MEMBER_ID WHERE 1=1";
        List<Object> params = new ArrayList<>();

        if (searchWord != null && !searchWord.trim().isEmpty()) {
            if ("blogName".equals(searchCategory)) {
                sql += " AND B.BLOG_TITLE LIKE ?";
            } else if ("blogIntro".equals(searchCategory)) {
                sql += " AND B.BLOG_DESC LIKE ?";
            } else if ("nickname".equals(searchCategory)) {
                sql += " AND M.NICKNAME LIKE ?";
            }
            params.add("%" + searchWord + "%");
        }

        return jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
    }

    // [3] 블로그 목록 조회 (페이징 + 검색)
    public List<BlogListRes> findBlogList(PageInfo pageInfo, String searchCategory, String searchWord) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("    SELECT ROWNUM AS RNUM, T.* FROM ( ");
        sql.append("        SELECT B.BLOG_ID, B.BLOG_TITLE, B.BLOG_DESC, B.PROFILE_IMG, M.NICKNAME ");
        sql.append("        FROM BLOG B JOIN MEMBER M ON B.MEMBER_ID = M.MEMBER_ID ");
        sql.append("        WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (searchWord != null && !searchWord.trim().isEmpty()) {
            if ("blogName".equals(searchCategory)) {
                sql.append(" AND B.BLOG_TITLE LIKE ? ");
            } else if ("blogIntro".equals(searchCategory)) {
                sql.append(" AND B.BLOG_DESC LIKE ? ");
            } else if ("nickname".equals(searchCategory)) {
                sql.append(" AND M.NICKNAME LIKE ? ");
            }
            params.add("%" + searchWord + "%");
        }

        // [수정] 정렬 기준 변경: 게시글 수(POST_COUNT) 내림차순 -> 생성일(CREATED_AT) 내림차순
        sql.append("        ORDER BY (SELECT COUNT(*) FROM POST P WHERE P.BLOG_ID = B.BLOG_ID) DESC, B.CREATED_AT DESC ");
        sql.append("    ) T WHERE ROWNUM <= ? ");
        sql.append(") WHERE RNUM >= ?");

        params.add(pageInfo.getEndRow());
        params.add(pageInfo.getStartRow());

        RowMapper<BlogListRes> mapper = (rs, rowNum) -> BlogListRes.builder()
                .blogId(rs.getLong("BLOG_ID"))
                .blogTitle(rs.getString("BLOG_TITLE"))
                .blogDesc(rs.getString("BLOG_DESC"))
                .profileImg(rs.getString("PROFILE_IMG"))
                .memberNickname(rs.getString("NICKNAME"))
                .build();

        return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
    }

    // [추가] 회원 ID로 블로그 ID 조회
    public Long findBlogIdByMemberId(Long memberId) {
        try {
            String sql = "SELECT BLOG_ID FROM BLOG WHERE MEMBER_ID = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, memberId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}