package com.post_it.blog.controller;

import com.post_it.blog.dto.blog.request.PostWriteReq;
import com.post_it.blog.dto.blog.response.BlogHomeRes;
import com.post_it.blog.dto.member.response.MemberRes;
import com.post_it.blog.service.BlogService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/{blogId}")
    public String blogHome(
            @PathVariable Long blogId,
            @RequestParam(value = "cp", defaultValue = "1", required = false) int currentPage,
            Model model,
            HttpSession session
    ) {
        MemberRes member = session.getAttribute("member") != null ? (MemberRes) session.getAttribute("member") : null;
        BlogHomeRes blogHomeRes = blogService.getBlogHomeData(blogId, currentPage);

        if (member != null && blogService.isMyBlog(blogId, member.getMemberId())) {
            model.addAttribute("isMyBlog", "ok");
        }

        if (blogHomeRes == null) {
            return "redirect:/";
        }

        model.addAttribute("targetUrl", "/blog/" + blogId);

        model.addAttribute("blogInfo", blogHomeRes.getBlogInfo());
        model.addAttribute("postList", blogHomeRes.getPostList());
        model.addAttribute("recentPostList", blogHomeRes.getRecentPostList());
        model.addAttribute("categoryList", blogHomeRes.getCategoryList());
        model.addAttribute("pageInfo", blogHomeRes.getPageInfo());
        model.addAttribute("totalPostCount", blogHomeRes.getTotalPostCount()); // [추가] 전체 게시글 수
        model.addAttribute("pageCss", "member_blog.css");
        return "blog/member_blog";
    }

    @GetMapping("/{blogId}/category/{categoryId}")
    public String blogCategory(
            @PathVariable Long blogId,
            @PathVariable Long categoryId,
            @RequestParam(value = "cp", defaultValue = "1", required = false) int currentPage,
            Model model,
            HttpSession session
    ) {
        MemberRes member = session.getAttribute("member") != null ? (MemberRes) session.getAttribute("member") : null;
        BlogHomeRes blogHomeRes = blogService.getBlogCategoryData(blogId, categoryId, currentPage);

        if (member != null && blogService.isMyBlog(blogId, member.getMemberId())) {
            model.addAttribute("isMyBlog", "ok");
        }

        if (blogHomeRes == null) {
            return "redirect:/";
        }

        model.addAttribute("targetUrl", "/blog/" + blogId + "/category/" + categoryId);

        model.addAttribute("blogInfo", blogHomeRes.getBlogInfo());
        model.addAttribute("postList", blogHomeRes.getPostList());
        model.addAttribute("recentPostList", blogHomeRes.getRecentPostList());
        model.addAttribute("categoryList", blogHomeRes.getCategoryList());
        model.addAttribute("pageInfo", blogHomeRes.getPageInfo());
        model.addAttribute("totalPostCount", blogHomeRes.getTotalPostCount()); // [추가]
        model.addAttribute("pageCss", "member_blog.css");
        return "blog/member_blog";
    }

    @GetMapping("/{blogId}/search")
    public String blogSearch(
            @PathVariable Long blogId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "cp", defaultValue = "1", required = false) int currentPage,
            Model model,
            HttpSession session
    ) {
        if (query == null || query.trim().isEmpty()) {
            return "redirect:/blog/" + blogId;
        }

        MemberRes member = session.getAttribute("member") != null ? (MemberRes) session.getAttribute("member") : null;
        BlogHomeRes blogHomeRes = blogService.getBlogSearchData(blogId, query, currentPage);

        if (member != null && blogService.isMyBlog(blogId, member.getMemberId())) {
            model.addAttribute("isMyBlog", "ok");
        }

        if (blogHomeRes == null) {
            return "redirect:/";
        }

        model.addAttribute("targetUrl", "/blog/" + blogId + "/search");
        model.addAttribute("query", query);

        model.addAttribute("blogInfo", blogHomeRes.getBlogInfo());
        model.addAttribute("postList", blogHomeRes.getPostList());
        model.addAttribute("recentPostList", blogHomeRes.getRecentPostList());
        model.addAttribute("categoryList", blogHomeRes.getCategoryList());
        model.addAttribute("pageInfo", blogHomeRes.getPageInfo());
        model.addAttribute("totalPostCount", blogHomeRes.getTotalPostCount()); // [추가]
        model.addAttribute("pageCss", "member_blog.css");
        return "blog/member_blog";
    }

    @GetMapping("/{blogId}/write")
    public String showWritePage(@PathVariable Long blogId, Model model, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) {
            return "redirect:/mem/login";
        }
        if (!blogService.isMyBlog(blogId, member.getMemberId())) {
            return "redirect:/blog/" + blogId;
        }

        model.addAttribute("categoryList", blogService.getCategoryList(blogId));
        model.addAttribute("blogId", blogId);
        model.addAttribute("pageCss", "write.css");
        return "blog/write";
    }

    @PostMapping("/upload/image")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = blogService.uploadImage(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("FAIL");
        }
    }

    @PostMapping("/{blogId}/write")
    @ResponseBody
    public ResponseEntity<String> writePost(
            @PathVariable Long blogId,
            @RequestBody PostWriteReq postWriteReq,
            HttpSession session
    ) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null || !blogService.isMyBlog(blogId, member.getMemberId())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        try {
            String cleanContent = sanitizeHtml(postWriteReq.getContent());
            postWriteReq.setContent(cleanContent);

            blogService.writePost(blogId, postWriteReq);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("게시글 저장 실패");
        }
    }

    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("pageCss", "post_detail.css");
        return "blog/post_detail";
    }

    private String sanitizeHtml(String content) {
        Safelist safelist = Safelist.relaxed();
        safelist.preserveRelativeLinks(true);
        safelist.removeProtocols("img", "src", "http", "https");
        safelist.addTags("iframe");
        safelist.addAttributes("iframe", "src", "width", "height", "allow", "allowfullscreen", "frameborder");
        safelist.addAttributes("img", "src", "width", "height", "style");
        safelist.addAttributes("p", "style", "class");
        safelist.addAttributes("span", "style", "class");
        safelist.addAttributes("div", "style", "class", "align");
        safelist.addAttributes("h1", "style", "class");
        safelist.addAttributes("h2", "style", "class");
        safelist.addAttributes("h3", "style", "class");

        return Jsoup.clean(content, safelist);
    }
}