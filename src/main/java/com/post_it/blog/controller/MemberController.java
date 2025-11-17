package com.post_it.blog.controller;

import com.post_it.blog.dto.member.request.SignUpReq;
import com.post_it.blog.service.MemberService;
import com.post_it.blog.validation.Validation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mem")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("pageCss", "signup.css");
        model.addAttribute("signUpReq", new SignUpReq());
        return "member/signup";
    }
    @PostMapping("/checkId")
    @ResponseBody
    public String checkUserId(@RequestParam String userId) {
        if (!Validation.idCheck(userId)) {
            return "DUPLICATE";
        }
        if (memberService.isUserIdAvailable(userId)) {
            return "DUPLICATE";
        }
        return "OK";
    }

    @PostMapping("/checkNick")
    @ResponseBody
    public String checkNickname(@RequestParam String nickname) {
        System.out.println(nickname);
        if (!Validation.nickCheck(nickname)) {
            return "FAIL";
        }
        if (memberService.isNicknameAvailable(nickname)) {
            System.out.println(1);
            return "DUPLICATE";
        }
        System.out.println(2);
        return "OK";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> processSignUp(@RequestBody SignUpReq signUpReq, HttpSession session) {
        if (!Validation.idCheck(signUpReq.getUserId()) || !Validation.passwordCheck(signUpReq.getPassword()) || !Validation.nameCheck(signUpReq.getName()) || !Validation.nickCheck(signUpReq.getNickname())) {
            return ResponseEntity.ok("FAIL");
        }
        signUpReq.setEmail((String) session.getAttribute("user_email"));
        session.removeAttribute("user_email");
        boolean result = memberService.signUp(signUpReq);
        if (!result) {
            return ResponseEntity.ok("FAIL");
        }
        session.setAttribute("nick", signUpReq.getNickname());
        return ResponseEntity.ok("SUCCESS");
    }

    @GetMapping("/welcome")
    public String showWelcomePage(Model model, HttpSession session) {
        if (session.getAttribute("nick") == null) {
            return "redirect:/";
        }
        model.addAttribute("pageCss", "welcome.css");
        model.addAttribute("nick", session.getAttribute("nick"));
        session.removeAttribute("nick");
        return "member/welcome";
    }
}
