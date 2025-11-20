package com.post_it.blog.service.impl;

import com.post_it.blog.dto.common.PageInfo;
import com.post_it.blog.dto.main.response.BestPostRes;
import com.post_it.blog.dto.main.response.BlogListRes;
import com.post_it.blog.repository.MainRepository;
import com.post_it.blog.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainServiceImpl implements MainService {

    private final MainRepository mainRepository;

    private static final int BLOGS_PER_PAGE = 4;
    private static final int PAGE_BLOCK = 5;

    @Override
    public List<BestPostRes> getBestPosts() {
        return mainRepository.findBestPosts();
    }

    @Override
    public List<BlogListRes> getBlogList(int currentPage, String searchCategory, String searchWord) {
        int totalCount = getTotalBlogCount(searchCategory, searchWord);
        PageInfo pageInfo = PageInfo.of(currentPage, totalCount, BLOGS_PER_PAGE, PAGE_BLOCK);
        return mainRepository.findBlogList(pageInfo, searchCategory, searchWord);
    }

    @Override
    public PageInfo getPageInfo(int currentPage, String searchCategory, String searchWord) {
        int totalCount = getTotalBlogCount(searchCategory, searchWord);
        return PageInfo.of(currentPage, totalCount, BLOGS_PER_PAGE, PAGE_BLOCK);
    }

    // [추가]
    @Override
    public Long getMyBlogId(Long memberId) {
        return mainRepository.findBlogIdByMemberId(memberId);
    }

    private int getTotalBlogCount(String searchCategory, String searchWord) {
        return mainRepository.countBlogList(searchCategory, searchWord);
    }
}