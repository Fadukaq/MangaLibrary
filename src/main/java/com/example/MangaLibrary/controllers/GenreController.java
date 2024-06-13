package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
public class GenreController {
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private GenreRepo genreRepo;
    private static final int PAGE_SIZE = 6;
    @GetMapping("/genre-list")
    public String genreList(Model model) {
        List<Genre> genreList = genreRepo.findAll();

        model.addAttribute("genreList", genreList);
        return "genre/manga-genre-list";
    }
    @GetMapping("/genre/add")
    public String genreAdd(Genre thisGenre) {
        return "genre/manga-genre-add";
    }
    @PostMapping("/genre/add")
    public String addPostGenre(@ModelAttribute("genre") @Valid Genre thisGenre, BindingResult result) {
        if (result.hasErrors()) {
            return "genre/manga-genre-add";
        }
        Genre existingGenre = genreRepo.findByGenreName(thisGenre.getGenreName());
        if (existingGenre != null) {
            result.rejectValue("genreName", "error.genre", "Жанр с таким именем уже существует");
            return "genre/manga-genre-add";
        }

        Genre genre = new Genre(thisGenre);
        genreRepo.save(genre);
        return "redirect:/manga";
    }
    @GetMapping("/genre/edit/{id}")
    public String genreEdit(@PathVariable("id") Long id, Model model) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid genre Id:" + id));

        model.addAttribute("genre", genre);
        return "genre/manga-genre-edit";
    }

    @PostMapping("/genre/edit/{id}")
    public String editPostGenre(@ModelAttribute("genre") @Valid Genre thisGenre, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "genre/manga-genre-edit";
        }

        Genre existingGenre = genreRepo.findByGenreName(thisGenre.getGenreName());
        if (existingGenre != null) {
            result.rejectValue("genreName", "error.genre", "Жанр с таким именем уже существует");
            return "genre/manga-genre-edit";
        }
        genreRepo.save(thisGenre);
        return "redirect:/manga";
    }

    @PostMapping("/genre/delete/{id}")
    public String GenrePostDelete(@PathVariable(value ="id") long id,Model model) {
        Genre genreToDelete = genreRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
        genreRepo.delete(genreToDelete);
        return "redirect:/manga";
    }

    @GetMapping("/genre/{genreName}")
    public String genreView(
            @PathVariable String genreName,
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending());
        Page<Manga> mangaPage = mangaRepo.findAllByGenreNamePagination(genreName, pageable);
        Genre getGenreId = genreRepo.findByGenreName(genreName);

        if (mangaPage.isEmpty()) {
            model.addAttribute("id", getGenreId.getId());
            model.addAttribute("genreName", genreName);
            model.addAttribute("noResults", true);
            model.addAttribute("genreName", genreName);
        } else
        {
            List<Manga> mangaList = mangaPage.getContent();

            model.addAttribute("noResults", false);
            model.addAttribute("genreName", genreName);
            model.addAttribute("id", getGenreId.getId());
            model.addAttribute("mangas", mangaList);
            model.addAttribute("page", mangaPage);
        }

        return "genre/manga-genre-view";
    }


}
