package com.post_it.blog.repository;

import com.post_it.blog.dto.member.request.SignUpReq;
import lombok.RequiredArgsConstructor;
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

    public boolean save(SignUpReq signUpReq) {
        String sql = "insert into member (ID, PWD, EMAIL, NAME, NICKNAME) values (?, ?, ?, ?, ?)";
        int result = jdbcTemplate.update(sql, signUpReq.getUserId(), signUpReq.getPassword(), signUpReq.getEmail(), signUpReq.getName(), signUpReq.getNickname());
        return result > 0;
    }
}
