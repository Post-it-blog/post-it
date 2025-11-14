package com.post_it.blog.controller;

import com.post_it.blog.dto.common.VerifyCode;
import com.post_it.blog.service.TestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final TestService testService;

    // 테스트 페이지
    @GetMapping
    public String showPage() {
        return "email/test";
    }

    // 인증코드 보내기
    @PostMapping("/send")
    @ResponseBody
    public String send(@RequestParam String email,
                       HttpSession session) {
        Long lastReq = (Long) session.getAttribute("lastEmailReq");

        long now = System.currentTimeMillis();

        if (lastReq != null && (now - lastReq) < 30000) { // 30초
            return "TOO_FAST";
        }

        session.setAttribute("lastEmailReq", now);

        String code = testService.createCode();
        VerifyCode verifyCode = new VerifyCode(code, now);

        session.setAttribute("testCode", verifyCode);

        testService.sendVerifyMail(email, code);

        return "OK";
    }

    // 인증코드 확인
    @PostMapping("/check")
    @ResponseBody
    public String check(@RequestParam String code,
                        HttpSession session) {

        VerifyCode verifyCode = (VerifyCode) session.getAttribute("testCode");
        if (verifyCode == null) return "FAIL";

        long diff = System.currentTimeMillis() - verifyCode.getCreatedAt();

        if (diff > 300000) { // 5분
            return "TIMEOUT";
        }

        if (!verifyCode.getCode().equals(code)) {
            return "FAIL";
        }

        return "OK";
    }
}