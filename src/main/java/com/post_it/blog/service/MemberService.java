package com.post_it.blog.service;

import com.post_it.blog.dto.member.request.SignUpReq;

public interface MemberService {
    boolean isUserIdAvailable(String userId);

    boolean isNicknameAvailable(String nickname);

    boolean isEmailAvailable(String email);

    boolean signUp(SignUpReq signUpReq);
}
