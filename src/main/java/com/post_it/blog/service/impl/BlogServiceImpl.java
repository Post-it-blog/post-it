package com.post_it.blog.service.impl;

import com.post_it.blog.dto.blog.request.PostWriteReq;
import com.post_it.blog.dto.blog.response.BlogHomeRes;
import com.post_it.blog.dto.blog.response.CategoryRes;
import com.post_it.blog.dto.blog.response.PostRes;
import com.post_it.blog.dto.common.PageInfo;
import com.post_it.blog.repository.BlogRepository;
import com.post_it.blog.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    private static final int POSTS_PER_PAGE = 5;
    private static final int PAGE_BLOCK = 5;
    private static final int MAX_PREVIEW_LENGTH = 100;

    @Override
    @Transactional(readOnly = true)
    public BlogHomeRes getBlogHomeData(Long blogId, int currentPage) {
        BlogHomeRes.BlogHomeResBuilder builder = BlogHomeRes.builder();
        builder.blogInfo(blogRepository.findBlogInfo(blogId));
        if (builder.build().getBlogInfo() == null) return null;

        int totalCount = blogRepository.countTotalPosts(blogId);
        builder.totalPostCount(totalCount);

        PageInfo pageInfo = PageInfo.of(currentPage, totalCount, POSTS_PER_PAGE, PAGE_BLOCK);
        builder.pageInfo(pageInfo);

        if (totalCount > 0) {
            List<PostRes> postList = blogRepository.findPostsWithPaging(blogId, pageInfo);
            builder.postList(createProcessedPostList(postList));
        } else {
            builder.postList(List.of());
        }

        builder.recentPostList(blogRepository.findRecentPosts(blogId));
        builder.categoryList(blogRepository.findCategoriesWithCount(blogId));

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogHomeRes getBlogCategoryData(Long blogId, Long categoryId, int currentPage) {
        BlogHomeRes.BlogHomeResBuilder builder = BlogHomeRes.builder();

        builder.blogInfo(blogRepository.findBlogInfo(blogId));
        if (builder.build().getBlogInfo() == null) return null;

        builder.totalPostCount(blogRepository.countTotalPosts(blogId));

        int categoryTotalCount = blogRepository.countTotalPostsByCategory(blogId, categoryId);

        PageInfo pageInfo = PageInfo.of(currentPage, categoryTotalCount, POSTS_PER_PAGE, PAGE_BLOCK);
        builder.pageInfo(pageInfo);

        if (categoryTotalCount > 0) {
            List<PostRes> postList = blogRepository.findPostsByCategoryWithPaging(blogId, categoryId, pageInfo);
            builder.postList(createProcessedPostList(postList));
        } else {
            builder.postList(List.of());
        }

        builder.recentPostList(blogRepository.findRecentPosts(blogId));
        builder.categoryList(blogRepository.findCategoriesWithCount(blogId));

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogHomeRes getBlogSearchData(Long blogId, String query, int currentPage) {
        BlogHomeRes.BlogHomeResBuilder builder = BlogHomeRes.builder();

        builder.blogInfo(blogRepository.findBlogInfo(blogId));
        if (builder.build().getBlogInfo() == null) return null;

        builder.totalPostCount(blogRepository.countTotalPosts(blogId));

        int searchTotalCount = blogRepository.countTotalPostsBySearch(blogId, query);

        PageInfo pageInfo = PageInfo.of(currentPage, searchTotalCount, POSTS_PER_PAGE, PAGE_BLOCK);
        builder.pageInfo(pageInfo);

        if (searchTotalCount > 0) {
            List<PostRes> postList = blogRepository.findPostsBySearchWithPaging(blogId, query, pageInfo);
            builder.postList(createProcessedPostList(postList));
        } else {
            builder.postList(List.of());
        }

        builder.recentPostList(blogRepository.findRecentPosts(blogId));
        builder.categoryList(blogRepository.findCategoriesWithCount(blogId));

        return builder.build();
    }

    @Override
    public boolean isMyBlog(Long blogId, Long memberId) {
        return blogRepository.isMyBlog(blogId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryRes> getCategoryList(Long blogId) {
        return blogRepository.findCategoriesWithCount(blogId);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("파일이 없습니다.");
        }
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String savedName = uuid + extension;
        File saveFile = new File(uploadPath, savedName);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }
        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
        return "/upload/" + savedName;
    }

    @Override
    public void writePost(Long blogId, PostWriteReq postWriteReq) {
        Long postId = blogRepository.savePost(blogId, postWriteReq);
        List<String> imageUrls = extractImageUrls(postWriteReq.getContent());
        if (!imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                blogRepository.savePostImage(postId, url, fileName);
            }
        }
    }

    private List<String> extractImageUrls(String content) {
        List<String> urls = new ArrayList<>();
        Pattern pattern = Pattern.compile("src\\s*=\\s*[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url.startsWith("/upload/")) {
                urls.add(url);
            }
        }
        return urls;
    }

    private List<PostRes> createProcessedPostList(List<PostRes> postList) {
        return postList.stream().map(post -> {
            String content = post.getContent();
            String plainText = content.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
            String preview;
            if (plainText.length() > MAX_PREVIEW_LENGTH) {
                preview = plainText.substring(0, MAX_PREVIEW_LENGTH) + "...";
            } else {
                preview = plainText;
            }
            return PostRes.builder()
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .categoryName(post.getCategoryName())
                    .createdAt(post.getCreatedAt())
                    .mainImageUrl(post.getMainImageUrl())
                    .content(content)
                    .contentPreview(preview)
                    .build();
        }).toList();
    }
}