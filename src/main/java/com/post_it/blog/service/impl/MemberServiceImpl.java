package com.post_it.blog.service.impl;

import com.post_it.blog.dto.member.request.FindPwdReq;
import com.post_it.blog.dto.member.request.LoginReq;
import com.post_it.blog.dto.member.request.SignUpReq;
import com.post_it.blog.dto.member.response.MemberRes;
import com.post_it.blog.repository.MemberRepository;
import com.post_it.blog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean isUserIdAvailable(String userId) {
        return memberRepository.existsByUserId(userId);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public boolean signUp(SignUpReq signUpReq) {
        signUpReq.setPassword(passwordEncoder.encode(signUpReq.getPassword()));
        Long result = memberRepository.save(signUpReq);
        if (result > 0) {
            Long blogId = memberRepository.addBlog(signUpReq, result);
            memberRepository.addCategory(blogId);
        }
        return result > 0;
    }

    @Override
    public MemberRes login(LoginReq loginReq) {
        MemberRes memberRes = memberRepository.findByUserId(loginReq.getUserId());
        if (memberRes != null) {
            if (passwordEncoder.matches(loginReq.getPassword(), memberRepository.findPwdByUserId(loginReq.getUserId()))) {
                return memberRes;
            }
        }
        return null;
    }

    @Override
    public String findPwd(FindPwdReq findPwdReq) {
        boolean result = memberRepository.findMemberInfo(findPwdReq);
        if (result) {
            return "OK";
        } else {
            return "NOT_FOUND";
        }
    }

    @Override
    public String findUserId(String email) {
        String result = memberRepository.findUserIdByEmail(email);
        return result.substring(0, result.length() - 2) + "**";
    }

    @Override
    public void changePwd(String newPwd, String userId) {
        memberRepository.updatePwd(passwordEncoder.encode(newPwd), userId);
    }
}
