package com.post_it.blog.controller;

import com.post_it.blog.dto.common.PageInfo;
import com.post_it.blog.dto.main.response.BestPostRes;
import com.post_it.blog.dto.main.response.BlogListRes;
import com.post_it.blog.dto.member.response.MemberRes;
import com.post_it.blog.service.MainService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping({"", "/", "/index"})
    public String showIndexPage(
            @RequestParam(value = "cp", defaultValue = "1") int currentPage,
            @RequestParam(value = "sc", required = false) String searchCategory,
            @RequestParam(value = "sw", required = false) String searchWord,
            Model model,
            HttpSession session // 세션 추가
    ) {
        // [추가] 로그인 상태 확인 및 내 블로그 ID 조회
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member != null) {
            Long myBlogId = mainService.getMyBlogId(member.getMemberId());
            model.addAttribute("myBlogId", myBlogId);
        }

        // 1. 베스트 게시글
        List<BestPostRes> bestPosts = mainService.getBestPosts();

        // 2. 블로그 목록
        List<BlogListRes> blogList = mainService.getBlogList(currentPage, searchCategory, searchWord);
        PageInfo pageInfo = mainService.getPageInfo(currentPage, searchCategory, searchWord);

        model.addAttribute("bestPosts", bestPosts);
        model.addAttribute("blogList", blogList);
        model.addAttribute("pageInfo", pageInfo);

        model.addAttribute("sc", searchCategory);
        model.addAttribute("sw", searchWord);

        model.addAttribute("pageCss", "index.css");
        return "main/index";
    }
}