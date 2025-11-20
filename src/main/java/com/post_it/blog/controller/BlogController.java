package com.post_it.blog.controller;

import com.post_it.blog.dto.blog.request.BlogInfoUpdateReq;
import com.post_it.blog.dto.blog.request.PostWriteReq;
import com.post_it.blog.dto.blog.response.BlogHomeRes;
import com.post_it.blog.dto.blog.response.BlogProfileRes;
import com.post_it.blog.dto.blog.response.PostDetailRes;
import com.post_it.blog.dto.comment.request.CommentUpdateReq;
import com.post_it.blog.dto.comment.request.CommentWriteReq;
import com.post_it.blog.dto.comment.response.CommentRes;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        MemberRes member = (MemberRes) session.getAttribute("member");
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
        model.addAttribute("totalPostCount", blogHomeRes.getTotalPostCount());
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
        MemberRes member = (MemberRes) session.getAttribute("member");
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
        model.addAttribute("totalPostCount", blogHomeRes.getTotalPostCount());
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

        MemberRes member = (MemberRes) session.getAttribute("member");
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
        model.addAttribute("totalPostCount", blogHomeRes.getTotalPostCount());
        model.addAttribute("pageCss", "member_blog.css");
        return "blog/member_blog";
    }

    /* 게시글 상세 페이지 */
    @GetMapping("/{blogId}/post/{postId}")
    public String postDetail(
            @PathVariable Long blogId,
            @PathVariable Long postId,
            Model model,
            HttpSession session
    ) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        Long memberId = member != null ? member.getMemberId() : null;

        // memberId 전달하여 좋아요 여부 확인
        PostDetailRes postDetail = blogService.getPostDetail(blogId, postId, memberId);
        if (postDetail == null) return "redirect:/blog/" + blogId;

        List<CommentRes> commentList = blogService.getCommentList(postId);
        int totalCommentCount = blogService.getCommentCount(postId);
        BlogHomeRes sidebarData = blogService.getBlogHomeData(blogId, 1);

        if (member != null) {
            if (blogService.isMyBlog(blogId, member.getMemberId())) {
                model.addAttribute("isMyBlog", "ok");
            }
            model.addAttribute("loginMemberId", member.getMemberId());
        }

        model.addAttribute("post", postDetail);
        model.addAttribute("commentList", commentList);
        model.addAttribute("totalCommentCount", totalCommentCount);
        model.addAttribute("blogInfo", sidebarData.getBlogInfo());
        model.addAttribute("categoryList", sidebarData.getCategoryList());
        model.addAttribute("recentPostList", sidebarData.getRecentPostList());
        model.addAttribute("totalPostCount", sidebarData.getTotalPostCount());
        model.addAttribute("pageCss", "post_detail.css");

        return "blog/post_detail";
    }

    /* 좋아요 토글 API */
    @PostMapping("/post/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        try {
            boolean liked = blogService.toggleLike(postId, member.getMemberId());
            int count = blogService.getLikeCount(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    /* 글쓰기 페이지 이동 */
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

    /* [수정됨] 게시글 수정 페이지 이동 */
    @GetMapping("/{blogId}/post/{postId}/edit")
    public String showEditPage(@PathVariable Long blogId, @PathVariable Long postId, Model model, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return "redirect:/mem/login";

        // [수정 포인트] getPostDetail 호출 시 memberId 인자 추가
        PostDetailRes post = blogService.getPostDetail(blogId, postId, member.getMemberId());

        if (post == null || !post.getMemberId().equals(member.getMemberId())) {
            return "redirect:/blog/" + blogId; // 권한 없음
        }

        model.addAttribute("categoryList", blogService.getCategoryList(blogId));
        model.addAttribute("blogId", blogId);
        model.addAttribute("post", post); // 기존 데이터 전달
        model.addAttribute("isEdit", true); // 수정 모드 플래그
        model.addAttribute("pageCss", "write.css");
        return "blog/write";
    }

    /* 에디터 이미지 업로드 (AJAX) */
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

    /* 게시글 저장 (AJAX) */
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

    /* 게시글 수정 처리 */
    @PostMapping("/{blogId}/post/{postId}/edit")
    @ResponseBody
    public ResponseEntity<String> editPost(
            @PathVariable Long blogId,
            @PathVariable Long postId,
            @RequestBody PostWriteReq req,
            HttpSession session
    ) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        try {
            String cleanContent = sanitizeHtml(req.getContent());
            req.setContent(cleanContent);
            blogService.editPost(postId, req, member.getMemberId());
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    /* 게시글 삭제 처리 */
    @PostMapping("/{blogId}/post/{postId}/delete")
    @ResponseBody
    public ResponseEntity<String> deletePost(@PathVariable Long blogId, @PathVariable Long postId, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        try {
            blogService.deletePost(postId, member.getMemberId());
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    /* 댓글 작성 */
    @PostMapping("/comment/write")
    @ResponseBody
    public ResponseEntity<String> writeComment(@RequestBody CommentWriteReq req, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        try {
            blogService.writeComment(req, member.getMemberId());
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    /* 댓글 수정 */
    @PostMapping("/comment/update")
    @ResponseBody
    public ResponseEntity<String> updateComment(@RequestBody CommentUpdateReq req, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        try {
            blogService.updateComment(req, member.getMemberId());
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    /* 댓글 삭제 */
    @PostMapping("/comment/delete")
    @ResponseBody
    public ResponseEntity<String> deleteComment(@RequestParam Long commentId, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        try {
            blogService.deleteComment(commentId, member.getMemberId());
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("FAIL");
        }
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

    // [추가] 블로그 관리 페이지
    @GetMapping("/{blogId}/profile")
    public String blogProfile(@PathVariable Long blogId, Model model, HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null) {
            return "redirect:/mem/login";
        }

        if (!blogService.isMyBlog(blogId, member.getMemberId())) {
            return "redirect:/blog/" + blogId;
        }

        BlogProfileRes blogProfile = blogService.getBlogProfile(blogId);
        model.addAttribute("blogProfile", blogProfile);
        model.addAttribute("pageCss", "blog_profile.css");

        return "blog/blog_profile";
    }

    // [추가] 블로그 기본 정보 수정
    @PostMapping("/{blogId}/profile/updateInfo")
    @ResponseBody
    public ResponseEntity<String> updateBlogInfo(
            @PathVariable Long blogId,
            @RequestBody BlogInfoUpdateReq req,
            HttpSession session
    ) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null || !blogService.isMyBlog(blogId, member.getMemberId())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        try {
            blogService.updateBlogInfo(blogId, req);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    // [추가] 블로그 프로필 이미지 수정
    @PostMapping("/{blogId}/profile/updateImg")
    @ResponseBody
    public ResponseEntity<String> updateBlogProfileImg(
            @PathVariable Long blogId,
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        if (member == null || !blogService.isMyBlog(blogId, member.getMemberId())) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }

        try {
            String imageUrl = blogService.updateBlogProfileImg(blogId, file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("FAIL");
        }
    }

    // [추가] 카테고리 관리 (추가/수정/삭제)
    @PostMapping("/{blogId}/category/add")
    @ResponseBody
    public ResponseEntity<String> addCategory(@PathVariable Long blogId, @RequestParam String categoryName) {
        try {
            blogService.addCategory(blogId, categoryName);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            // 10개 초과 시 에러 메시지 반환
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/{blogId}/category/update")
    @ResponseBody
    public ResponseEntity<String> updateCategory(@RequestParam Long categoryId, @RequestParam String categoryName) {
        blogService.updateCategory(categoryId, categoryName);
        return ResponseEntity.ok("SUCCESS");
    }

    @PostMapping("/{blogId}/category/delete")
    @ResponseBody
    public ResponseEntity<String> deleteCategory(@RequestParam Long categoryId) {
        try {
            blogService.deleteCategory(categoryId);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패 (게시글이 있는 카테고리는 삭제할 수 없습니다.)");
        }
    }
}