package com.post_it.blog.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class VerifyCode {
    private String code;
    private long createdAt;
}
