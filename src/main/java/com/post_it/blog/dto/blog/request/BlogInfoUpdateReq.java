package com.post_it.blog.dto.blog.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BlogInfoUpdateReq {
    private String blogTitle;
    private String blogDesc;
}