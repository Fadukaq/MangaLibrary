package com.example.MangaLibrary.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String home(Model model) {
        return "main/home";
    }
    @GetMapping("/about")
    public String about(Model model)
    {
        return "main/about";
    }
    @GetMapping("/faq")
    public String faq(Model model)
    {

        return "main/faq";
    }
    @GetMapping("/contact-us")
    public String contactUs(Model model)
    {

        return "main/contact-us";
    }
}