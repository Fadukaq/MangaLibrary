package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.MangaService;
import com.example.MangaLibrary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private UserRepo userRepo;
    @Autowired
    private MangaLibraryManager mangaLibraryManager;
    private static final int PAGE_SIZE = 6;
    @Autowired
    UserService userService;
    @Autowired
    MangaService mangaService;
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
        model.addAttribute("genres", genres);

        return "manga/manga-add";
    }
    @PostMapping("/manga/add")
    public String addManga(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult bindingResult, Model model) throws IOException {
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        model.addAttribute("maxYear", maxYear);
        model.addAttribute("genres", genres);

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

    @PostMapping("/manga/page/{mangaId}")
    public String viewMangaPage(@PathVariable Long mangaId, Model model) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        if (mangaOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            String[] mangaPages = manga.getMangaPages().split(","); // Получаем список страниц манги
            String mangaFolder = manga.getMangaName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String[] mangaPagesWithFullPath = new String[mangaPages.length];

            for (int i = 0; i < mangaPages.length; i++) {
                mangaPagesWithFullPath[i] = "/images/mangas/" + mangaFolder + "/" + mangaPages[i];
                manga.setMangaPages(mangaPagesWithFullPath[i]);
            }

            model.addAttribute("mangaPages", mangaPagesWithFullPath);
            model.addAttribute("id", mangaId);

            return "manga/manga-page";
        } else {
            model.addAttribute("errorMessage", "Такой манги не найдено!");
            return "main/error";
        }
    }

    @GetMapping("/manga/{id}")
    public String mangaDetails(@PathVariable(value = "id") long id, Model model) {
        Optional<Manga> optionalManga = mangaRepo.findById(id);
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);

            boolean isInReadingList = user.getMangaReading().contains(String.valueOf(id));
            boolean isInWantToReadList = user.getMangaWantToRead().contains(String.valueOf(id));
            boolean isInRecitedList = user.getMangaRecited().contains(String.valueOf(id));
            boolean isInReadStoppedList = user.getMangaStoppedReading().contains(String.valueOf(id));

            model.addAttribute("manga", manga);
            model.addAttribute("isInReadingList", isInReadingList);
            model.addAttribute("isInWantToReadList", isInWantToReadList);
            model.addAttribute("isInRecitedList", isInRecitedList);
            model.addAttribute("isInReadStoppedList", isInReadStoppedList);
            return "manga/manga-details";
        } else {
            model.addAttribute("errorMessage", "Такой манги не найдено!");
            return "main/error";
        }
    }
    @PostMapping("/manga/add-to-list")
    public String addMangaToList(@RequestParam("listType") String listType,
                                    @RequestParam("mangaId") Long mangaId,
                                    RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);
        if(user!=null){
            userService.deleteMangaFromUserList(user, mangaId);

            userService.addMangaToList(user, listType, mangaId, redirectAttributes);

            userRepo.save(user);
        }
        return "redirect:/manga/" + mangaId;
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
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            MangaForm mangaForm = new MangaForm();
            mangaForm.setManga(manga);

            model.addAttribute("manga", manga);
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("mangaForm", mangaForm); // Передаем объект MangaForm в модель
            return "manga/manga-edit";
        } else {
            return "redirect:/manga";
        }
    }
    @PostMapping("/manga/edit/{id}")
    public String mangaPostUpdate(@PathVariable("id") long id, @ModelAttribute("mangaForm") @Valid MangaForm mangaForm,
                                    BindingResult bindingResult, Model model) {
        List<Long> genreIds = mangaForm.getManga().getGenres().stream()
                .map(Genre::getId)
                .toList();
        if(!mangaService.isValidUpdateMangaForm(mangaForm, bindingResult)) {
            int maxYear = Year.now().getValue();
            List<Genre> genres = genreRepo.findAll();
            model.addAttribute("id", id);
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("mangaForm", mangaForm);
            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }
            return "manga/manga-edit";
        }
        mangaService.updateManga(id,mangaForm);

        return "redirect:/manga";
    }
    @GetMapping("/search")
    public String postSearch(@RequestParam("q") String searchQuery,
                                @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                Model model) {
        if (!searchQuery.isEmpty()) {
            Page<Manga> foundMangas = mangaRepo.findByMangaNameContainingIgnoreCase(searchQuery, PageRequest.of(page - 1,PAGE_SIZE, Sort.by("id").descending()));
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

            return "manga/manga-search";
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
