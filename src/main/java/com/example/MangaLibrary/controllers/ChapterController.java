package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.helper.manga.ChapterForm;
import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.ChapterRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.ChapterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class ChapterController {
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    ChapterService chapterService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    ChapterRepo chapterRepo;
    @GetMapping("/manga/{mangaId}/chapter/add")
    public String chapterAddGet(@PathVariable Long mangaId, ChapterForm chapterForm, Model model) {
        Optional<Manga> manga = mangaRepo.findById(mangaId);
        if(manga.isPresent()) {
            chapterForm.setManga(manga.get());
            model.addAttribute("chapterForm", chapterForm);
            model.addAttribute("mangaId", mangaId);
            return "manga/manga-add-chapter";
        }else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }

    @PostMapping("/manga/{mangaId}/chapter/add")
    public String chapterAddPost(@PathVariable Long mangaId, @Valid @ModelAttribute ChapterForm chapterForm,
                                    BindingResult result, RedirectAttributes redirectAttributes, Model model) throws IOException {
        if (!chapterService.isValidChapterFormAdd(chapterForm, result)) {
            model.addAttribute("mangaId", mangaId);
            return "manga/manga-add-chapter";
        }

        Optional<Manga> thisManga = mangaRepo.findById(mangaId);
        if (thisManga.isPresent()) {
            Manga manga = thisManga.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);

            chapterService.addChapter(chapterForm, manga, user);
            return "redirect:/manga/" + mangaId;
        } else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }

    @GetMapping("/manga/{mangaId}/chapter/{chapterId}")
    public String viewChapter(@PathVariable Long mangaId, @PathVariable Long chapterId,
                              @RequestParam(defaultValue = "1") int page,
                              Principal principal,
                              Model model,
                              HttpServletRequest request) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isPresent() && chapterOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = chapterOptional.get();
            User user = userRepo.findByUserName(principal.getName());
            String[] chapterImageFileNames = chapter.getChapterPages().split(",");
            String[] chapterImageUrls;

            if(user.getUserSettings().getReadStyle().equals("scroll-down")){
                chapterImageFileNames = chapter.getChapterPages().split(",");

                chapterImageUrls = new String[chapterImageFileNames.length];
                for (int i = 0; i < chapterImageFileNames.length; i++) {
                    chapterImageUrls[i] = String.format("/images/mangas/%s/chapters/%s/%s", mangaId, chapter.getId(), chapterImageFileNames[i]);
                }
                model.addAttribute("chapterImageUrls", chapterImageUrls);
            }else{
                int imagesPerPage;
                if(user.getUserSettings().getPageStyle().equals("book-view")){
                    imagesPerPage = 2;
                }else{
                    imagesPerPage = 1;
                }
                int startIndex = (page - 1) * imagesPerPage;
                int endIndex = Math.min(startIndex + imagesPerPage, chapterImageFileNames.length);

                chapterImageUrls = Arrays.stream(Arrays.copyOfRange(chapterImageFileNames, startIndex, endIndex))
                        .map(fileName -> String.format("/images/mangas/%s/chapters/%s/%s", mangaId, chapter.getId(), fileName))
                        .toArray(String[]::new);

                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", (int) Math.ceil((double) chapterImageFileNames.length / imagesPerPage));
            }
            String currentUrl = request.getRequestURI();
            List<Chapter> chapterList = chapterService.findAllChaptersByMangaId(mangaId);
            Chapter previousChapter = chapterService.findPreviousChapter(mangaId, chapterId);
            Chapter nextChapter = chapterService.findNextChapter(mangaId, chapterId);

            model.addAttribute("user", user);
            model.addAttribute("manga", manga);
            model.addAttribute("chapter", chapter);
            model.addAttribute("nextChapter", nextChapter);
            model.addAttribute("chapterList", chapterList);
            model.addAttribute("previousChapter", previousChapter);
            model.addAttribute("chapterImageUrls", chapterImageUrls);
            model.addAttribute("currentUrl", currentUrl);

            return "manga/manga-chapter-view";
        } else {
            model.addAttribute("errorMessage", "Такої манги або глави не знайдено!");
            return "main/error";
        }
    }

    @GetMapping("/manga/{mangaId}/chapter/edit/{chapterId}")
    public String chapterEditGet(@PathVariable Long mangaId, @PathVariable Long chapterId, Model model) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isPresent() && chapterOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = chapterOptional.get();
            String[] chapterImageFileNames = chapter.getChapterPages().split(",");

            String[] chapterImageUrls = new String[chapterImageFileNames.length];
            for (int i = 0; i < chapterImageFileNames.length; i++) {
                chapterImageUrls[i] = String.format("/images/mangas/%d/chapters/%d/%s", mangaId, chapterId, chapterImageFileNames[i]);
            }

            ChapterForm chapterForm = new ChapterForm();
            chapterForm.setManga(manga);
            chapterForm.setChapter(chapter);
            model.addAttribute("chapterForm", chapterForm);
            model.addAttribute("manga", manga);
            model.addAttribute("chapter", chapter);
            model.addAttribute("chapterImageUrls", chapterImageUrls);
            return "manga/manga-chapter-edit";
        } else {
            model.addAttribute("errorMessage", "Такої глави, або манги не знайдено!");
            return "main/error";
        }
    }

    @PostMapping("/manga/{mangaId}/chapter/edit/{chapterId}")
    public String chapterEditPost(@PathVariable Long mangaId, @PathVariable Long chapterId,
                                  @Valid @ModelAttribute("chapterForm") ChapterForm chapterForm,
                                  BindingResult result, RedirectAttributes redirectAttributes,
                                  Model model) throws IOException {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isEmpty() || chapterOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Такої глави, або манги не знайдено!");
            return "main/error";
        }

        Manga manga = mangaOptional.get();
        Chapter chapter = chapterOptional.get();

        if (!chapterService.isValidChapterForm(chapterForm , result)) {
            chapterService.addChapterDataToModel(model, manga, chapter, chapterForm);
            return "manga/manga-chapter-edit";
        }

        try {
            chapterService.editChapter(chapterForm, manga, chapter);
            redirectAttributes.addFlashAttribute("message", "Главу успішно оновлено");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Помилка під час оновлення глави: " + e.getMessage());
            return "main/error";
        }

        return "redirect:/manga/"+mangaId;
    }

    @PostMapping("/manga/{mangaId}/chapter/delete/{chapterId}")
    public String deleteChapter(@PathVariable Long mangaId, @PathVariable Long chapterId, RedirectAttributes redirectAttributes) {
        try {
            chapterService.deleteChapter(mangaId, chapterId);
            redirectAttributes.addFlashAttribute("message", "Главу успішно видалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення глави: " + e.getMessage());
        }
        return "redirect:/manga/"+mangaId+"#chapters";
    }
}
