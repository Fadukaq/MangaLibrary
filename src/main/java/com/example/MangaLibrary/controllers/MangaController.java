package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.manga.ChapterForm;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import com.example.MangaLibrary.service.ChapterService;
import com.example.MangaLibrary.service.MangaService;
import com.example.MangaLibrary.service.UserService;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
public class MangaController {
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private GenreRepo genreRepo;
    @Autowired
    private UserSettingsRepo userSettingsRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AuthorRepo authorRepo;
    @Autowired
    private MangaLibraryManager mangaLibraryManager;
    private static final int PAGE_SIZE = 6;
    @Autowired
    UserService userService;
    @Autowired
    MangaService mangaService;
    @Autowired
    ChapterService chapterService;
    @Autowired
    ChapterRepo chapterRepo;
    @GetMapping("/manga")
    public String mangas(
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            Model model
    ) {

        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending());
        Page<Manga> mangaPage = mangaRepo.findAll(pageable);
        List<Manga> mangaList = mangaPage.getContent();
        int totalPages = mangaPage.getTotalPages();
        if (page > totalPages) {
            return "redirect:/manga?page=" + totalPages;
        }
        model.addAttribute("mangas", mangaList);
        model.addAttribute("page", mangaPage);
        return "manga/manga-main";
    }

    @GetMapping("/manga/add")
    public String mangaAdd(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "manga/manga-add";
        }
        int maxYear = Year.now().getValue();
        model.addAttribute("maxYear", maxYear);
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();

        model.addAttribute("authors", authors);
        model.addAttribute("genres", genres);
        return "manga/manga-add";
    }
    @PostMapping("/manga/add")
    public String addManga(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult bindingResult, Model model) throws IOException {
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();

        model.addAttribute("maxYear", maxYear);
        model.addAttribute("genres", genres);
        model.addAttribute("authors", authors);

        if (!mangaService.isValidAddMangaForm(mangaForm, bindingResult)) {
            return "manga/manga-add";
        }

        try {
            mangaService.saveManga(mangaForm);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("manga.mangaName", "error.manga", e.getMessage());
            return "manga/manga-add";
        }

        return "redirect:/manga";
    }
    @GetMapping("/manga/{mangaId}/chapter/add")
    public String chapterAddGet(@PathVariable Long mangaId, ChapterForm chapterForm, Model model) {
        Manga manga = mangaRepo.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id: " + mangaId));
        chapterForm.setManga(manga);
        model.addAttribute("chapterForm", chapterForm);
        model.addAttribute("mangaId", mangaId);
        return "manga/manga-add-chapter";
    }

    @PostMapping("/manga/{mangaId}/chapter/add")
    public String chapterAddPost(@PathVariable Long mangaId,@Valid @ModelAttribute ChapterForm chapterForm,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes,
                                    Model model) throws IOException {
        if (!chapterService.isValidChapterForm(chapterForm,result)) {
            model.addAttribute("mangaId", mangaId);
            return "manga/manga-add-chapter";
        }
        Optional<Manga> thisManga = mangaRepo.findById(mangaId);
        if(thisManga.isPresent()) {
            Manga manga = thisManga.get();
            boolean chapterExists = chapterRepo.existsByMangaIdAndTitle(mangaId, chapterForm.getChapter().getTitle());
            if (chapterExists) {
                result.rejectValue("chapter.title", "error.title", "Глава с таким названием уже существует");
                model.addAttribute("mangaId", mangaId);
                return "manga/manga-add-chapter";
            }

            chapterService.addChapter(chapterForm, manga);
            redirectAttributes.addFlashAttribute("message", "Chapter added successfully");
        }
        return "redirect:/manga";
    }

    @GetMapping("/manga/{mangaId}/chapter/{chapterId}")
    public String viewChapter(@PathVariable Long mangaId, @PathVariable Long chapterId, Model model) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isPresent() && chapterOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = chapterOptional.get();
            String mangaFolder = chapterService.cleanStringForUrl(manga.getMangaName());
            String[] chapterImageFileNames = chapter.getChapterPages().split(",");

            String[] chapterImageUrls = new String[chapterImageFileNames.length];
            for (int i = 0; i < chapterImageFileNames.length; i++) {
                chapterImageUrls[i] = String.format("/images/mangas/%s/chapters/%s/%s", mangaFolder, chapter.getTitle(), chapterImageFileNames[i]);
            }

            model.addAttribute("manga", manga);
            model.addAttribute("chapter", chapter);
            model.addAttribute("chapterImageUrls", chapterImageUrls);

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
            String mangaFolder = chapterService.cleanStringForUrl(manga.getMangaName());
            String[] chapterImageFileNames = chapter.getChapterPages().split(",");

            String[] chapterImageUrls = new String[chapterImageFileNames.length];
            for (int i = 0; i < chapterImageFileNames.length; i++) {
                chapterImageUrls[i] = String.format("/images/mangas/%s/chapters/%s/%s", mangaFolder, chapter.getTitle(), chapterImageFileNames[i]);
            }

            ChapterForm chapterForm = new ChapterForm();
            chapterForm.setManga(manga);
            chapterForm.setChapter(chapter);
            chapterForm.getChapterImage().setExistingImages(Arrays.asList(chapterImageUrls));

            model.addAttribute("chapterForm", chapterForm);
            model.addAttribute("manga", manga);
            model.addAttribute("chapter", chapter);
            model.addAttribute("chapterImageUrls", chapterImageUrls);
            return "manga/manga-chapter-edit";
        } else {
            model.addAttribute("errorMessage", "Такої манги або глави не знайдено!");
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

        if (!mangaOptional.isPresent() || !chapterOptional.isPresent()) {
            model.addAttribute("errorMessage", "Такой манги или главы не найдено!");
            return "main/error";
        }

        Manga manga = mangaOptional.get();
        Chapter chapter = chapterOptional.get();

        if (!chapterService.isValidChapterForm(chapterForm, result)) {
            addChapterDataToModel(model, manga, chapter, chapterForm);
            return "manga/manga-chapter-edit";
        }

        boolean chapterExists = chapterRepo.existsByMangaIdAndTitleAndIdNot(mangaId, chapterForm.getChapter().getTitle(), chapterId);
        if (chapterExists) {
            result.rejectValue("chapter.title", "error.title", "Глава с таким названием уже существует");
            addChapterDataToModel(model, manga, chapter, chapterForm);
            return "manga/manga-chapter-edit";
        }

        try {
            chapterService.editChapter(chapterForm, manga, chapter);
            redirectAttributes.addFlashAttribute("message", "Глава успешно обновлена");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении главы: " + e.getMessage());
            return "main/error";
        }

        return "redirect:/manga/" + mangaId + "#chapters";
    }

    private void addChapterDataToModel(Model model, Manga manga, Chapter chapter, ChapterForm chapterForm) {
        String mangaFolder = chapterService.cleanStringForUrl(manga.getMangaName());
        String[] chapterImageFileNames = chapter.getChapterPages().split(",");
        String[] chapterImageUrls = new String[chapterImageFileNames.length];
        for (int i = 0; i < chapterImageFileNames.length; i++) {
            chapterImageUrls[i] = String.format("/images/mangas/%s/chapters/%s/%s", mangaFolder, chapter.getTitle(), chapterImageFileNames[i]);
        }

        model.addAttribute("manga", manga);
        model.addAttribute("chapter", chapter);
        model.addAttribute("chapterImageUrls", chapterImageUrls);
        model.addAttribute("chapterForm", chapterForm);
    }


    @PostMapping("/manga/{mangaId}/chapter/delete/{chapterId}")
    public String deleteChapter(@PathVariable Long mangaId, @PathVariable Long chapterId, RedirectAttributes redirectAttributes) {
        try {
            chapterService.deleteChapter(mangaId, chapterId);
            redirectAttributes.addFlashAttribute("message", "Глава успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления главы: " + e.getMessage());
        }
        return "redirect:/manga/" + mangaId + "#chapters";
    }

    @GetMapping("/manga/{id}")
    public String getManga(@PathVariable Long id, ModelMap model, Principal principal) {
        Optional<Manga> optionalManga = mangaRepo.findById(id);
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            List<Chapter> chapters = chapterRepo.findByMangaId(manga.getId());

            UserSettings userSettings = new UserSettings();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);

            userSettings = userSettingsRepo.findByUser(user);

            String translatedStatus = mangaService.getMangaTranslatedStatus(manga.getMangaStatus());

            MangaService.addMangaStatusAttributes(user, id, model);

            model.addAttribute("manga", manga);
            model.addAttribute("translatedMangaStatus", translatedStatus);
            model.addAttribute("userSettings", userSettings);
            model.addAttribute("chapters", chapters);
            return "manga/manga-details";
        } else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }
    @PostMapping("/manga/add-to-list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addMangaToList(@RequestParam("listType") String listType,
                                                                @RequestParam("mangaId") Long mangaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);
        Map<String, Object> response = new HashMap<>();

        if(user != null) {
            userService.deleteMangaFromUserList(user, mangaId);
            userService.addMangaToList(user, listType, mangaId);
            userRepo.save(user);
            response.put("success", true);
            response.put("message", "Манга успішно додана у список");
        } else {
            response.put("success", false);
            response.put("message", "Користувач не знайдений");
        }

        return ResponseEntity.ok(response);
    }
    @PostMapping("/manga/delete/{id}")
    public String MangaPostDelete(@PathVariable(value ="id") long id,Model model) {
        Manga mangaToDelete = mangaRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
        mangaRepo.delete(mangaToDelete);

        String mangaName = mangaToDelete.getMangaName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
        mangaService.deleteFolder(mangaName);

        Iterable<User> usersIterable = userRepo.findAll();
        List<User> users = new ArrayList<>();
        usersIterable.forEach(users::add);

        userService.deleteMangaFromUsersList(users, id);
        return "redirect:/manga";
    }

    @GetMapping("/manga/edit/{id}")
    public String mangaEdit(@PathVariable("id") long id, Model model) {

        Optional<Manga> optionalManga = mangaRepo.findById(id);
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();


        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            MangaForm mangaForm = new MangaForm();
            mangaForm.setManga(manga);

            model.addAttribute("manga", manga);
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
            model.addAttribute("mangaForm", mangaForm);
            return "manga/manga-edit";
        } else {
            return "redirect:/manga";
        }
    }
    @PostMapping("/manga/edit/{id}")
    public String mangaPostUpdate(@PathVariable("id") long id,
                                    @ModelAttribute("mangaForm") @Valid MangaForm mangaForm,
                                    BindingResult bindingResult, Model model) {
        List<Long> genreIds = mangaForm.getManga().getGenres().stream()
                .map(Genre::getId)
                .toList();

        if(!mangaService.isValidUpdateMangaForm(mangaForm, bindingResult)) {
            int maxYear = Year.now().getValue();
            List<Genre> genres = genreRepo.findAll();
            List<Author> authors = authorRepo.findAll();

            model.addAttribute("id", id);
            model.addAttribute("manga", mangaForm.getManga());
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
            model.addAttribute("mangaForm", mangaForm);
            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }
            return "manga/manga-edit";
        }
        mangaService.updateManga(id,mangaForm);
        return "redirect:/manga/"+id;
    }
        @GetMapping("/search")
    public String search(@RequestParam("q") String searchQuery,
                            @RequestParam("type") String searchType,
                            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                            Model model) {
        if (!searchQuery.isEmpty()) {
            Page<Manga> foundMangas;
            switch (searchType) {
                case "title":
                    foundMangas = mangaRepo.findByMangaNameContainingIgnoreCase(searchQuery, PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending()));
                    break;
                case "genre":
                    Genre genre = genreRepo.findByGenreName(searchQuery);
                    foundMangas = mangaRepo.findByGenresGenreNameContainingIgnoreCase(searchQuery, PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending()));
                    model.addAttribute("genreId", genre.getId());
                    break;
                case "releaseYear":
                    foundMangas = mangaRepo.findByReleaseYear(searchQuery, PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending()));
                    break;
                case "mangaStatus":
                    foundMangas = mangaRepo.findByMangaStatusContainingIgnoreCase(searchQuery, PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending()));
                    break;
                default:
                    model.addAttribute("errorMessage", "Немає такого типу пошуку!");
                    return "main/error";
            }
            List<Manga> mangaList = foundMangas.getContent();
            if (mangaList.isEmpty()) {
                model.addAttribute("noResults", true);
                model.addAttribute("searchQuery", searchQuery);

            } else {
                model.addAttribute("noResults", false);
                model.addAttribute("mangas", mangaList);
                model.addAttribute("currentPage", foundMangas.getNumber());
                model.addAttribute("totalPages", foundMangas.getTotalPages());
                model.addAttribute("searchQuery", searchQuery);
            }
            model.addAttribute("searchType", searchType);
            return "manga/manga-search-result";
        }
        return "redirect:/manga";
    }

    @GetMapping("/random")
    public String getRandomMangaId(Model model) {

        Long randomMangaId = mangaService.getRandomMangaId();

        model.addAttribute("randomMangaId", randomMangaId);
        if(randomMangaId==null) {
            return "redirect:/manga";
        }
        return "redirect:/manga/" + randomMangaId;
    }
}
