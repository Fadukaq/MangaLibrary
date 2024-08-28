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
import java.util.Optional;

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
            result.rejectValue("genreName", "error.genre", "Такий жанр вже існує");
            return "genre/manga-genre-add";
        }

        Genre genre = new Genre(thisGenre);
        genreRepo.save(genre);
        return "redirect:/manga";
    }
    @GetMapping("/genre/edit/{id}")
    public String genreEdit(@PathVariable("id") Long id, Model model) {
        Optional<Genre> genre = genreRepo.findById(id);
        if(genre.isPresent()) {
            model.addAttribute("genre", genre.get());
            return "genre/manga-genre-edit";
        }else {
            model.addAttribute("errorMessage", "Такого жанру не знайдено!");
            return "main/error";
        }
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
        Optional<Genre> genreToDelete = genreRepo.findById(id);
        if (genreToDelete.isPresent()){
            genreRepo.delete(genreToDelete.get());
            return "redirect:/admin-dashboard?tab=genresTable";
        } else {
            model.addAttribute("errorMessage", "Такого жанру не знайдено!");
            return "main/error";
        }
    }
    @GetMapping("/genres-filter")
    @ResponseBody
    public List<Genre> getAllGenres() {
        return genreRepo.findAll();
    }
}
