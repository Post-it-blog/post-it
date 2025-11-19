package com.post_it.blog.service;

import com.post_it.blog.dto.mypage.response.MyPageMemberInfoRes;

public interface MyPageService {
    boolean matchPwd(Long memberId, String rawPwd);

    MyPageMemberInfoRes getMemberInfo(Long memberId);

    void updatePassword(Long memberId, String newPwd);

    boolean duplicateNick(String newNickname);

    void updateNickname(Long memberId, String newNickname);
}
