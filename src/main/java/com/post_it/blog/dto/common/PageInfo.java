package com.post_it.blog.dto.common;

import lombok.Getter;

@Getter
public class PageInfo {
    private int currentPage; // 현재 페이지 번호
    private int startPage;   // 페이지 블록의 시작 페이지 (예: 1~5 페이지 블록이면 1)
    private int endPage;     // 페이지 블록의 끝 페이지 (예: 1~5 페이지 블록이면 5)
    private int totalPage;   // 전체 페이지 수 (총 게시글 ÷ 한 페이지당 게시글 수, 올림)
    private boolean prev;    // 이전 페이지 블록 존재 여부 (1페이지면 false)
    private boolean next;    // 다음 페이지 블록 존재 여부 (마지막 페이지면 false)
    private int startRow;    // DB 조회용 시작 행 번호 (현재 페이지 기준)
    private int endRow;      // DB 조회용 끝 행 번호 (현재 페이지 기준)

    private PageInfo(int currentPage, int totalCount, int postsPerPage, int pageBlock) {
        // 전체 페이지 수 계산 (총 게시글 ÷ 한 페이지당 게시글 수, 올림)
        this.totalPage = (int) Math.ceil((double) totalCount / postsPerPage);

        // 현재 페이지가 유효 범위를 벗어나면 보정
        if (currentPage < 1) currentPage = 1;           // 최소 1페이지
        if (currentPage > totalPage) currentPage = totalPage; // 최대 totalPage
        this.currentPage = currentPage;

        // DB 조회용 시작/끝 행 계산
        // ex) 현재 페이지 4, 한 페이지당 10개 → startRow=31, endRow=40
        this.startRow = (currentPage - 1) * postsPerPage + 1;
        this.endRow = currentPage * postsPerPage;

        // 현재 페이지 블록 계산
        // ex) 페이지 단위 5 → 1~5, 6~10, 11~15 ...
        int currentBlock = (int) Math.ceil((double) currentPage / pageBlock);
        this.startPage = (currentBlock - 1) * pageBlock + 1;
        this.endPage = Math.min(startPage + pageBlock - 1, totalPage);

        // 이전/다음 버튼 존재 여부
        this.prev = currentPage > 1;        // 현재 페이지가 1보다 크면 이전 버튼 존재
        this.next = currentPage < totalPage; // 현재 페이지가 마지막 페이지보다 작으면 다음 버튼 존재
    }

    public static PageInfo of(int currentPage, int totalCount, int postsPerPage, int pageBlock) {
        return new PageInfo(currentPage, totalCount, postsPerPage, pageBlock);
    }
}
