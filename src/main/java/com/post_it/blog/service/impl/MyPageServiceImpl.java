package com.post_it.blog.service.impl;

import com.post_it.blog.dto.mypage.response.MyPageMemberInfoRes;
import com.post_it.blog.repository.MyPageRepository;
import com.post_it.blog.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {
    private final PasswordEncoder passwordEncoder;
    private final MyPageRepository myPageRepository;
    @Override
    public boolean matchPwd(Long memberId, String rawPwd) {
        String pwd = myPageRepository.getPwd(memberId);
        if (pwd != null) {
            return passwordEncoder.matches(rawPwd, pwd);
        }
        return false;
    }

    @Override
    public MyPageMemberInfoRes getMemberInfo(Long memberId) {
        MyPageMemberInfoRes myPageMemberInfoRes = myPageRepository.getMemberInfo(memberId);
        String userId = myPageMemberInfoRes.getUserId();
        myPageMemberInfoRes.setUserId(userId.substring(0, userId.length() - 2) + "**");
        String email = myPageMemberInfoRes.getEmail();
        myPageMemberInfoRes.setEmail(email.substring(0, email.indexOf("@") - 2) + "**" + email.substring(email.indexOf("@")));
        return myPageMemberInfoRes;
    }

    @Override
    public void updatePassword(Long memberId, String newPwd) {
        myPageRepository.updatePassword(memberId, passwordEncoder.encode(newPwd));
    }

    @Override
    public boolean duplicateNick(String newNickname) {
        return myPageRepository.checkNick(newNickname);
    }

    @Override
    public void updateNickname(Long memberId, String newNickname) {
        myPageRepository.updateNickname(memberId, newNickname);
    }

    @Override
    public void deleteMember(Long memberId) {
        myPageRepository.deleteMember(memberId);
    }
}
