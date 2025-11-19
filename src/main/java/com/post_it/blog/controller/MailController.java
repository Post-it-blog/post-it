package com.post_it.blog.controller;

import com.post_it.blog.dto.common.VerifyCode;
import com.post_it.blog.dto.member.request.FindPwdReq;
import com.post_it.blog.service.MailService;
import com.post_it.blog.service.MemberService;
import com.post_it.blog.validation.Validation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class MailController {

    private final MailService mailService;
    private final MemberService memberService;

    // 인증코드 보내기
    @PostMapping("/send")
    @ResponseBody
    public String send(@RequestParam String email,
                       HttpSession session) {
        if (!Validation.emailCheck(email)) {
            return "FAIL";
        }
        if (memberService.isEmailAvailable(email)) {
            return "DUPLICATE";
        }

        Long lastReq = (Long) session.getAttribute("lastEmailReq");

        long now = System.currentTimeMillis();

        if (lastReq != null && (now - lastReq) < 20000) { // 20초
            return "TOO_FAST";
        }

        session.setAttribute("lastEmailReq", now);

        String code = mailService.createCode();
        VerifyCode verifyCode = new VerifyCode(code, now);

        session.setAttribute("verifyCode", verifyCode);
        session.setAttribute("temporary_email", email);
        mailService.sendVerifyMail(email, code);

        return "OK";
    }

    // 인증코드 확인
    @PostMapping("/check")
    @ResponseBody
    public String check(@RequestParam String code, @RequestParam String email,
                        HttpSession session) {
        System.out.println(code);
        System.out.println(email);
        if (session.getAttribute("temporary_email") != null && !session.getAttribute("temporary_email").equals(email)) {
            return "NOT_MATCHED_EMAIL";
        }
        session.removeAttribute("temporary_email");
        VerifyCode verifyCode = (VerifyCode) session.getAttribute("verifyCode");
        if (verifyCode == null) return "FAIL";

        long diff = System.currentTimeMillis() - verifyCode.getCreatedAt();

        if (diff > 300000) { // 5분
            return "TIMEOUT";
        }

        if (!verifyCode.getCode().equals(code)) {
            return "FAIL";
        }

        session.setAttribute("user_email", email);
        session.setAttribute("emailAuthStatus", "OK");
        session.setAttribute("emailAuthExpireAt", System.currentTimeMillis() + 180000); // 3분
        return "OK";
    }

    @PostMapping("/findId")
    @ResponseBody
    public String findId(@RequestParam String email,
                       HttpSession session) {
        if (!Validation.emailCheck(email)) {
            return "FAIL";
        }

        Long lastReq = (Long) session.getAttribute("lastEmailReq");

        long now = System.currentTimeMillis();

        if (lastReq != null && (now - lastReq) < 20000) { // 20초
            return "TOO_FAST";
        }

        session.setAttribute("lastEmailReq", now);

        String code = mailService.createCode();
        VerifyCode verifyCode = new VerifyCode(code, now);

        session.setAttribute("verifyCode", verifyCode);
        session.setAttribute("temporary_email", email);
        mailService.sendVerifyMail(email, code);

        return "OK";
    }

    @PostMapping("/findIdCheck")
    @ResponseBody
    public String findIdCheck(@RequestParam String code, @RequestParam String email,
                              HttpSession session) {
        System.out.println(code);
        System.out.println(email);
        if (session.getAttribute("temporary_email") != null && !session.getAttribute("temporary_email").equals(email)) {
            return "NOT_MATCHED_EMAIL";
        }
        session.removeAttribute("temporary_email");
        VerifyCode verifyCode = (VerifyCode) session.getAttribute("verifyCode");
        if (verifyCode == null) return "FAIL";

        long diff = System.currentTimeMillis() - verifyCode.getCreatedAt();

        if (diff > 300000) { // 5분
            return "TIMEOUT";
        }

        if (!verifyCode.getCode().equals(code)) {
            return "FAIL";
        }

        String findUserId = memberService.findUserId(email);
        session.setAttribute("findUserId", findUserId);

        session.setAttribute("emailAuthStatus", "OK");
        session.setAttribute("emailAuthExpireAt", System.currentTimeMillis() + 180000); // 3분
        return "OK";
    }

    @PostMapping("/findPwd")
    @ResponseBody
    public String findPwd(@RequestParam String email, @RequestParam String userId,
                       HttpSession session) {
        if (!Validation.emailCheck(email) || !Validation.idCheck(userId)) {
            return "FAIL";
        }

        Long lastReq = (Long) session.getAttribute("lastEmailReq");

        long now = System.currentTimeMillis();

        if (lastReq != null && (now - lastReq) < 20000) { // 20초
            return "TOO_FAST";
        }

        FindPwdReq findPwdReq = new FindPwdReq(userId, email);
        String result = memberService.findPwd(findPwdReq);

        if (result.equals("NOT_FOUND")) return result;

        session.setAttribute("lastEmailReq", now);

        String code = mailService.createCode();
        VerifyCode verifyCode = new VerifyCode(code, now);

        session.setAttribute("verifyCode", verifyCode);
        session.setAttribute("temporary_email", email);
        mailService.sendVerifyMail(email, code);

        return "OK";
    }

    @PostMapping("/findPwdCheck")
    @ResponseBody
    public String findPwdCheck(@RequestParam String code, @RequestParam String userId, @RequestParam String email,
                              HttpSession session) {
        if (!Validation.idCheck(userId) || !Validation.emailCheck(email)) {
            return "NO";
        }
        if (session.getAttribute("temporary_email") != null && !session.getAttribute("temporary_email").equals(email)) {
            return "NOT_MATCHED";
        }
        session.removeAttribute("temporary_email");
        VerifyCode verifyCode = (VerifyCode) session.getAttribute("verifyCode");
        if (verifyCode == null) return "FAIL";

        long diff = System.currentTimeMillis() - verifyCode.getCreatedAt();

        if (diff > 300000) { // 5분
            return "TIMEOUT";
        }

        if (!verifyCode.getCode().equals(code)) {
            return "FAIL";
        }

        session.setAttribute("userIdP", userId);

        session.setAttribute("emailAuthStatus", "OK");
        session.setAttribute("emailAuthExpireAt", System.currentTimeMillis() + 180000); // 3분
        return "OK";
    }
}