package com.post_it.blog.dto.member.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PwdChangeReq {
    private String newPwd;
    private String newPwdConfirm;
}
