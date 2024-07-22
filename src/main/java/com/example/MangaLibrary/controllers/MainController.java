package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.repo.ChapterRepo;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.service.MangaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MainController {
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private ChapterRepo chapterRepo;
    @Autowired
    MangaService mangaService;
    @Autowired
    private GenreRepo genreRepo;
    @GetMapping("/")
    public String home(Model model) {
        List<Manga> newMangaList = mangaRepo.findMangasByIdDesc().stream().limit(8).collect(Collectors.toList());
        List<Chapter> latestChapters = chapterRepo.findLatestChapters(PageRequest.of(0, 16));
        List<Map<String, Object>> mangaWithNewChapters = new ArrayList<>();
        for (Chapter chapter : latestChapters) {
            Manga manga = chapter.getManga();

            Map<String, Object> mangaInfo = new HashMap<>();
            mangaInfo.put("mangaId", manga.getId());
            mangaInfo.put("mangaName", manga.getMangaName());
            mangaInfo.put("mangaPosterImg", manga.getMangaPosterImg());
            mangaInfo.put("chapterId", chapter.getId());
            mangaInfo.put("chapterTitle", chapter.getTitle());
            mangaInfo.put("creationTime", chapter.getCreationTime());

            mangaWithNewChapters.add(mangaInfo);
        }
        List<Map<String, Object>> newMangaListMap = new ArrayList<>();
        for (Manga manga : newMangaList) {
            Map<String, Object> mangaMap = new HashMap<>();
            mangaMap.put("id", manga.getId());
            mangaMap.put("mangaName", manga.getMangaName());
            mangaMap.put("mangaPosterImg", manga.getMangaPosterImg());

            List<Map<String, Object>> chaptersMap = manga.getChapter().stream()
                    .map(chapter -> {
                        Map<String, Object> chapterMap = new HashMap<>();
                        chapterMap.put("id", chapter.getId());
                        chapterMap.put("chapterTitle", chapter.getTitle());
                        return chapterMap;
                    })
                    .collect(Collectors.toList());
            mangaMap.put("chapters", chaptersMap);

            List<Map<String, Object>> genresMap = manga.getGenres().stream()
                    .map(genre -> {
                        Map<String, Object> genreMap = new HashMap<>();
                        genreMap.put("id", genre.getId());
                        genreMap.put("name", genre.getGenreName());
                        return genreMap;
                    })
                    .collect(Collectors.toList());
            mangaMap.put("genres", genresMap);

            newMangaListMap.add(mangaMap);
        }

        List<Genre> allGenres = genreRepo.findAll();
        Map<String, List<Manga>> mangaByGenre = new LinkedHashMap<>();
        Set<Long> addedMangaIds = new HashSet<>();
        int maxGenres = 3;
        int maxResults = 11;
        int minResults = 10;

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
            if (uniqueMangaList.size() >= minResults) {
                mangaByGenre.put(genre.getGenreName(), uniqueMangaList);
            }
        }
        if (mangaByGenre.size() < maxGenres) {
            for (Genre genre : allGenres) {
                if (mangaByGenre.size() >= maxGenres) {
                    break;
                }
                if (!mangaByGenre.containsKey(genre.getGenreName())) {
                    List<Manga> allMangaList = mangaRepo.findMangasByGenreName(genre.getGenreName(), PageRequest.of(0, maxResults).withSort(Sort.by(Sort.Order.desc("id"))));
                    List<Manga> uniqueMangaList = allMangaList.stream()
                            .filter(manga -> addedMangaIds.add(manga.getId()))
                            .collect(Collectors.toList());
                    if (!uniqueMangaList.isEmpty()) {
                        mangaByGenre.put(genre.getGenreName(), uniqueMangaList);
                    }
                }
            }
        }

        for (List<Manga> mangaList : mangaByGenre.values()) {
            for (Manga manga : mangaList) {
                manga.setMangaStatus(mangaService.getMangaTranslatedStatus(manga.getMangaStatus()));
            }
        }

        model.addAttribute("latestUpdatesList", mangaWithNewChapters);
        model.addAttribute("NewMangaListMap", newMangaListMap);
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