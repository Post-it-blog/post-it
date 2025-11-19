package com.post_it.blog.controller;

import com.post_it.blog.dto.member.request.LoginReq;
import com.post_it.blog.dto.member.request.PwdChangeReq;
import com.post_it.blog.dto.member.request.SignUpReq;
import com.post_it.blog.dto.member.response.MemberRes;
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

    /* 회원가입 시작 */
    @GetMapping("/signup")
    public String showSignUpForm(Model model, HttpSession session) {
        if (session.getAttribute("member") != null) {
            return "redirect:/";
        }
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
    /* 회원가입 끝 */

    /* 로그인 시작 */
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        if (session.getAttribute("member") != null) {
            return "redirect:/";
        }
        model.addAttribute("loginReq", new LoginReq());
        model.addAttribute("pageCss", "login.css");
        return "member/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("loginReq") LoginReq loginReq, Model model, HttpSession session) {
        if (Validation.idCheck(loginReq.getUserId()) && Validation.passwordCheck(loginReq.getPassword())) {
            MemberRes memberRes = memberService.login(loginReq);

            if (memberRes != null) {
                session.setAttribute("member", memberRes);
                session.setMaxInactiveInterval(30*60);
                return "redirect:/";
            }
        }
        model.addAttribute("pageCss", "login.css");
        model.addAttribute("errorMsg", "아이디 또는 비밀번호를 다시 확인해주세요.");
        return "member/login";
    }
    /* 로그인 끝 */

    /* 아이디 찾기 시작 */
    @GetMapping("/findId")
    public String showFindIdPage(Model model) {
        model.addAttribute("pageCss", "find-id.css");
        return "member/find-id";
    }

    @GetMapping("/findMemberId")
    public String showFindIdSuccessPage(Model model, HttpSession session) {
        model.addAttribute("pageCss", "find-id-result.css");
        model.addAttribute("userId", session.getAttribute("findUserId"));
        session.removeAttribute("findUserId");
        return "member/find-id-result";
    }
    /* 아이디 찾기 끝 */

    /* 비밀번호 찾기 시작 */
    @GetMapping("/findPwd")
    public String showFindPwdPage(Model model) {
        model.addAttribute("pageCss", "find-pwd.css");
        return "member/find-pwd";
    }

    @GetMapping("/pwdChange")
    public String showPwdChangePage(Model model, HttpSession session) {
        if (session.getAttribute("userIdP") == null) {
            model.addAttribute("pageCss", "find-pwd.css");
            return "member/find-pwd";
        }
        model.addAttribute("pwdChangeReq", new PwdChangeReq());
        model.addAttribute("pageCss", "reset-password.css");
        return "member/reset-password";
    }

    @PostMapping("/pwdChange")
    public String processPwdChange(@ModelAttribute("pwdChangeReq") PwdChangeReq pwdChangeReq, Model model, HttpSession session) {
        System.out.println(pwdChangeReq.getNewPwd());
        System.out.println(pwdChangeReq.getNewPwdConfirm());
        if (!Validation.passwordCheck(pwdChangeReq.getNewPwd())) {
            System.out.println(1);
            model.addAttribute("pwdChangeReq", new PwdChangeReq());
            model.addAttribute("pageCss", "reset-password.css");
            model.addAttribute("errorMsg", "비밀번호: 8~16자의 영문자, 숫자, 특수문자의 조합을 사용해 주세요.");
            return "member/reset-password";
        }
        if (!pwdChangeReq.getNewPwd().equals(pwdChangeReq.getNewPwdConfirm())) {
            System.out.println(2);
            model.addAttribute("pwdChangeReq", new PwdChangeReq());
            model.addAttribute("pageCss", "reset-password.css");
            model.addAttribute("errorMsg", "비밀번호 확인: 비밀번호가 일치하지 않습니다.");
            return "member/reset-password";
        }
        System.out.println(3);
        memberService.changePwd(pwdChangeReq.getNewPwd(), (String) session.getAttribute("userIdP"));
        session.removeAttribute("userIdP");
        model.addAttribute("close", "close");
        return "member/reset-password";
    }
    /* 비밀번호 찾기 끝 */
}
