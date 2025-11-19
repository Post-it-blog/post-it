package com.post_it.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    @GetMapping({"", "/", "/index"})
    public String showIndexPage(Model model) {
        model.addAttribute("pageCss", "index.css");
        return "main/index";
    }
}
