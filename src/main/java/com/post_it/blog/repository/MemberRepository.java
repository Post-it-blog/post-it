package com.post_it.blog.repository;

import com.post_it.blog.dto.member.request.FindPwdReq;
import com.post_it.blog.dto.member.request.SignUpReq;
import com.post_it.blog.dto.member.response.MemberRes;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public boolean existsByUserId(String userId) {
        String sql = "select count(*) from member where id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean existsByNickname(String nickname) {
        String sql = "select count(*) from member where nickname = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nickname);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String sql = "select count(*) from member where email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public Long save(SignUpReq signUpReq) {
        String sql = "insert into member (ID, PWD, EMAIL, NAME, NICKNAME) values (?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql, signUpReq.getUserId(), signUpReq.getPassword(), signUpReq.getEmail(), signUpReq.getName(), signUpReq.getNickname());
        return jdbcTemplate.queryForObject("select MEMBER_SEQ.CURRVAL from dual", Long.class);
    }


    public MemberRes findByUserId(String userId) {
        System.out.println(userId);
        String sql = "select m.MEMBER_ID, PWD, NICKNAME, ROLE, m.CREATED_AT, BLOG_ID from member m join blog b on m.member_id = b.member_id where ID = ?";
        try {
            MemberRes memberRes = jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new MemberRes(
                            rs.getLong("MEMBER_ID"),
                            rs.getNString("NICKNAME"),
                            rs.getString("ROLE"),
                            rs.getTimestamp("CREATED_AT").toLocalDateTime(),
                            rs.getLong("BLOG_ID")
                        )
                    , userId);
            return memberRes;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public String findPwdByUserId(String userId) {
        String sql = "select PWD from member where ID = ?";
        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }

    public boolean findMemberInfo(FindPwdReq findPwdReq) {
        String sql = "select count(*) from member where id = ? and email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, findPwdReq.getUserId(), findPwdReq.getEmail());
        return count != null && count > 0;
    }

    public String findUserIdByEmail(String email) {
        String sql = "select id from member where email = ?";
        return jdbcTemplate.queryForObject(sql, String.class, email);
    }

    public Long addBlog(SignUpReq signUpReq, Long memberId) {
        String defaultTitleDesc = signUpReq.getNickname() + "님의 블로그 입니다.";
        String sql = "insert into blog (MEMBER_ID, BLOG_TITLE, BLOG_DESC, PROFILE_IMG, PROFILE_IMG_ORIGINAL, PROFILE_IMG_SIZE, PROFILE_IMG_TYPE) values (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, memberId, defaultTitleDesc, defaultTitleDesc, "/upload/no_user_image.png", "no_user_image.png", null, "image/png");
        return jdbcTemplate.queryForObject("select BLOG_SEQ.CURRVAL from dual", Long.class);
    }

    public void updatePwd(String newPwd, String userId) {
        String sql = "update member set pwd = ? where id = ?";
        jdbcTemplate.update(sql, newPwd, userId);
    }

    public void addCategory(Long blogId) {
        String sql = "insert into category (BLOG_ID, CATEGORY_NAME) values (?, ?)";
        jdbcTemplate.update(sql, blogId, "포스트잇");
    }
}
