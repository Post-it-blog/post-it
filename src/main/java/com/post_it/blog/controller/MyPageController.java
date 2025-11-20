package com.post_it.blog.controller;

import com.post_it.blog.dto.member.response.MemberRes;
import com.post_it.blog.dto.mypage.response.MyPageMemberInfoRes;
import com.post_it.blog.service.MemberService;
import com.post_it.blog.service.MyPageService;
import com.post_it.blog.validation.Validation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /* 마이페이지 진입 전 패스워드 확인 시작 */
    @GetMapping
    public String mpPasswordConfirm(Model model, HttpSession session) {
        if (session.getAttribute("member") == null) {
            return "redirect:/";
        }
        model.addAttribute("pageCss", "mp-before.css");
        return "mypage/mp-before";
    }
    @PostMapping
    public String processPasswordConfirm(@RequestParam String pwd, Model model, HttpSession session) {
        if (session.getAttribute("member") == null) {
            return "redirect:/";
        }
        if (!Validation.passwordCheck(pwd)) {
            model.addAttribute("pageCss", "mp-before.css");
            model.addAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
            return "mypage/mp-before";
        }

        MemberRes member = (MemberRes) session.getAttribute("member");
        if (myPageService.matchPwd(member.getMemberId(), pwd)) {
            session.setAttribute("memberId", member.getMemberId());
            return "redirect:/mypage/home";
        }

        model.addAttribute("pageCss", "mp-before.css");
        model.addAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
        return "mypage/mp-before";
    }
    /* 마이페이지 진입 전 패스워드 확인 끝 */

    /* 마이페이지 시작 */
    @GetMapping("/home")
    public String showMyPageHomePage(Model model, HttpSession session) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/";
        }

        Long memberId = (Long) session.getAttribute("memberId");
        MyPageMemberInfoRes myPageMemberInfoRes = myPageService.getMemberInfo(memberId);
        model.addAttribute("name", myPageMemberInfoRes.getName());
        model.addAttribute("nickname", myPageMemberInfoRes.getNickname());
        model.addAttribute("userId", myPageMemberInfoRes.getUserId());
        model.addAttribute("email", myPageMemberInfoRes.getEmail());
        model.addAttribute("profileImg", myPageMemberInfoRes.getProfileImg());
        model.addAttribute("pageCss", "mypage-home.css");
        return "mypage/home";
    }

    @PostMapping("/pwdChange")
    public String processPwdChange(Model model, HttpSession session, @RequestParam String pwd, @RequestParam String newPwd, @RequestParam String newPwdConfirm) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/";
        }

        Long memberId = (Long) session.getAttribute("memberId");
        if (!Validation.passwordCheck(newPwd)) {
            model.addAttribute("passwordErrorMsg", "비밀번호: 8~16자의 영문자, 숫자, 특수문자의 조합을 사용해 주세요.<br>(사용 가능한 특수문자: !@#$%^&*()_+-=)");
        } else if (!myPageService.matchPwd(memberId, pwd)) {
            model.addAttribute("passwordErrorMsg", "기존 비밀번호가 일치하지 않습니다.");
        } else if (!newPwd.equals(newPwdConfirm)) {
            model.addAttribute("passwordErrorMsg", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        } else {
            myPageService.updatePassword(memberId, newPwd);
            return "redirect:/mem/logout";
        }

        MyPageMemberInfoRes myPageMemberInfoRes = myPageService.getMemberInfo(memberId);
        model.addAttribute("name", myPageMemberInfoRes.getName());
        model.addAttribute("nickname", myPageMemberInfoRes.getNickname());
        model.addAttribute("userId", myPageMemberInfoRes.getUserId());
        model.addAttribute("email", myPageMemberInfoRes.getEmail());
        model.addAttribute("profileImg", myPageMemberInfoRes.getProfileImg());
        model.addAttribute("pageCss", "mypage-home.css");
        return "mypage/home";
    }

    @PostMapping("/nickChange")
    public String processNickChange(Model model, HttpSession session, @RequestParam String newNickname) {
        if (session.getAttribute("memberId") == null) {
            return "redirect:/";
        }

        Long memberId = (Long) session.getAttribute("memberId");
        if (!Validation.nickCheck(newNickname)) {
            model.addAttribute("nicknameErrorMsg", "닉네임: 4~20자의 한글, 영문 대/소문자를 사용해 주세요. (사용 가능한 특수문자 -, _)");
        } else if (myPageService.duplicateNick(newNickname)) {
            model.addAttribute("nicknameErrorMsg", "이미 사용중인 닉네임 입니다.");
        } else {
            myPageService.updateNickname(memberId, newNickname);
            MemberRes member = (MemberRes) session.getAttribute("member");
            member.setNickname(newNickname);
            session.setAttribute("member", member);
        }

        MyPageMemberInfoRes myPageMemberInfoRes = myPageService.getMemberInfo(memberId);
        model.addAttribute("name", myPageMemberInfoRes.getName());
        model.addAttribute("nickname", myPageMemberInfoRes.getNickname());
        model.addAttribute("userId", myPageMemberInfoRes.getUserId());
        model.addAttribute("email", myPageMemberInfoRes.getEmail());
        model.addAttribute("profileImg", myPageMemberInfoRes.getProfileImg());
        model.addAttribute("pageCss", "mypage-home.css");
        return "mypage/home";
    }

    @PostMapping("/delete")
    public String processMemberDelete(HttpSession session) {
        MemberRes member = (MemberRes) session.getAttribute("member");
        myPageService.deleteMember(member.getMemberId());
        session.invalidate();
        return "redirect:/";
    }
    /* 마이페이지 끝 */
}
