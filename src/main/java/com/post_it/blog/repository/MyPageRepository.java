package com.post_it.blog.repository;

import com.post_it.blog.dto.mypage.response.MyPageMemberInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyPageRepository {
    private final JdbcTemplate jdbcTemplate;

    public String getPwd(Long memberId) {
        String sql = "select pwd from member where member_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, memberId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public MyPageMemberInfoRes getMemberInfo(Long memberId) {
        String sql = "select NAME, NICKNAME, ID, EMAIL, PROFILE_IMG, BLOG_ID from member m join blog b on m.member_id = b.member_id where m.member_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                        new MyPageMemberInfoRes(
                              rs.getNString(1),
                              rs.getNString(2),
                              rs.getString(3),
                              rs.getString(4),
                              rs.getNString(5),
                              rs.getLong(6)
                        )
                    , memberId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updatePassword(Long memberId, String encode) {
        String sql = "update member set pwd = ? where member_id = ?";
        jdbcTemplate.update(sql, encode, memberId);
    }

    public boolean checkNick(String newNickname) {
        String sql = "select count(*) from member where nickname = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, newNickname);
        return count != null && count > 0;
    }

    public void updateNickname(Long memberId, String newNickname) {
        String sql = "update member set nickname = ? where member_id = ?";
        jdbcTemplate.update(sql, newNickname, memberId);
    }

    public void deleteMember(Long memberId) {
        String sql = "delete member where member_id = ?";
        jdbcTemplate.update(sql, memberId);
    }
}
