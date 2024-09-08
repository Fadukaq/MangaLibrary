package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @Autowired
    MainService mainService;
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestUpdatesList", mainService.getLatestMangaWithChapters(16));
        model.addAttribute("NewMangaListMap", mainService.getNewMangaList(8));
        model.addAttribute("mangaByGenre", mainService.getMangaByGenre(3, 11, 7));
        return "main/home";
    }

    @GetMapping("/about")
    public String about(Model model)
    {
        return "main/about";
    }
    @GetMapping("/faq")
    public String faq(Model model) {
        return "main/faq";
    }
    @GetMapping("/faqdlc")
    public String faqdlc(Model model) {
        return "main/faqdlc";
    }
    @GetMapping("/dmca")
    public String dmca(Model model) {
        return "main/dmca";
    }
    @GetMapping("/contact-us")
    public String contactUs(Model model) {
        return "main/contact-us";
    }
}