package com.post_it.blog.service;

import com.post_it.blog.dto.member.request.FindPwdReq;
import com.post_it.blog.dto.member.request.LoginReq;
import com.post_it.blog.dto.member.request.SignUpReq;
import com.post_it.blog.dto.member.response.MemberRes;

public interface MemberService {
    boolean isUserIdAvailable(String userId);

    boolean isNicknameAvailable(String nickname);

    boolean isEmailAvailable(String email);

    boolean signUp(SignUpReq signUpReq);

    MemberRes login(LoginReq loginReq);

    String findPwd(FindPwdReq findPwdReq);

    String findUserId(String email);

    void changePwd(String newPwd, String userId);
}
