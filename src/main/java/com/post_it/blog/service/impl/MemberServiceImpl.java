package com.post_it.blog.service.impl;

import com.post_it.blog.dto.member.request.SignUpReq;
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
        return memberRepository.save(signUpReq);
    }
}
