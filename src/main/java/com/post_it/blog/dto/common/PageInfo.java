package com.post_it.blog.dto.common;

import lombok.Getter;

@Getter
public class PageInfo {
    private int currentPage; // 현재 페이지 번호
    private int startPage;   // 페이지 블록의 시작 페이지
    private int endPage;     // 페이지 블록의 끝 페이지
    private int totalPage;   // 전체 페이지 수
    private boolean prev;    // 이전 블록 존재 여부
    private boolean next;    // 다음 블록 존재 여부
    private int startRow;    // DB 조회용 시작 행
    private int endRow;      // DB 조회용 끝 행
    private int totalCount;  // 전체 게시글 수

    private PageInfo(int currentPage, int totalCount, int postsPerPage, int pageBlock) {
        this.totalCount = totalCount; // 전체 게시글 수 저장

        // 1. 전체 페이지 수 계산
        this.totalPage = (int) Math.ceil((double) totalCount / postsPerPage);

        if (this.totalPage == 0) {
            this.totalPage = 1;
        }

        // 2. 현재 페이지 보정
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPage) currentPage = totalPage;
        this.currentPage = currentPage;

        // 3. DB 조회용 시작/끝 행 계산
        this.startRow = (currentPage - 1) * postsPerPage + 1;
        this.endRow = currentPage * postsPerPage;

        // 4. 페이지 블록 계산
        int currentBlock = (int) Math.ceil((double) currentPage / pageBlock);

        this.startPage = (currentBlock - 1) * pageBlock + 1;
        this.endPage = Math.min(startPage + pageBlock - 1, totalPage);

        // 5. 이전/다음 버튼 존재 여부
        this.prev = this.startPage > 1;
        this.next = this.endPage < this.totalPage;
    }

    public static PageInfo of(int currentPage, int totalCount, int postsPerPage, int pageBlock) {
        return new PageInfo(currentPage, totalCount, postsPerPage, pageBlock);
    }
}