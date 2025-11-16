package com.post_it.blog.service.impl;

import com.post_it.blog.repository.MemberRepository;
import com.post_it.blog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public boolean isUserIdAvailable(String userId) {
        return memberRepository.existsByUserId(userId);
    }
}
