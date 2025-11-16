package com.post_it.blog.controller;

import com.post_it.blog.service.MemberService;
import com.post_it.blog.validation.Validation;
import lombok.RequiredArgsConstructor;
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
}
