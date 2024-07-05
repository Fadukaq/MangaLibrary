package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.service.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainController {
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    MangaService mangaService;
    @Autowired
    private GenreRepo genreRepo;
    @GetMapping("/")
    public String home(Model model) {
        List<Manga> NewMangaList = mangaRepo.findMangasByIdDesc().stream().limit(5).collect(Collectors.toList());

        List<Genre> allGenres = genreRepo.findAll();
        Map<String, List<Manga>> mangaByGenre = new LinkedHashMap<>();
        Set<Long> addedMangaIds = new HashSet<>();

        int maxGenres = 5;
        int maxResults = 8;

        for (Genre genre : allGenres) {
            if (mangaByGenre.size() >= maxGenres) {
                break;
            }

            List<Manga> allMangaList = mangaRepo.findMangasByGenreName(genre.getGenreName(), PageRequest.of(0, maxResults * 3).withSort(Sort.by(Sort.Order.desc("id"))));
            List<Manga> uniqueMangaList = new ArrayList<>();

            for (Manga manga : allMangaList) {
                if (addedMangaIds.add(manga.getId())) {
                    uniqueMangaList.add(manga);
                    if (uniqueMangaList.size() >= maxResults) {
                        break;
                    }
                }
            }

            if (!uniqueMangaList.isEmpty()) {
                mangaByGenre.put(genre.getGenreName(), uniqueMangaList);
            }
        }
        mangaByGenre.entrySet().removeIf(entry -> entry.getValue().size() < maxResults);
        for (Map.Entry<String, List<Manga>> entry : mangaByGenre.entrySet()) {
            for (Manga manga : entry.getValue()) {
                    manga.setMangaStatus(mangaService.getMangaTranslatedStatus(manga.getMangaStatus()));
            }
        }
        model.addAttribute("NewMangaList", NewMangaList);
        model.addAttribute("mangaByGenre", mangaByGenre);
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