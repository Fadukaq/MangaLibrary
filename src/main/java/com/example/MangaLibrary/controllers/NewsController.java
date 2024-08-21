package com.example.MangaLibrary.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NewsController {

    @GetMapping("/news")
    public String newsList(Model model) {

        return "news/news-list";
    }
    @GetMapping("/news/{id}")
    public String newsDetails(Model model) {

        return "news/news-details";
    }

    @GetMapping("/news/add")
    public String newsAddGet(Model model) {

        return "news/new-add";
    }
    @PostMapping("/news/add")
    public String newsAddPost(Model model) {

        return "news/news-add";
    }

    @GetMapping("/news/edit/{id}")
    public String newsEditGet(Model model) {

        return "news/new-edit";
    }
    @PostMapping("/news/add/{id}")
    public String newsEditPost(Model model) {

        return "news/news-edit";
    }
}
