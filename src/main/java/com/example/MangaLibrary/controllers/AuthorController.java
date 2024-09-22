package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.Subscription;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.SubscriptionRepo;
import com.example.MangaLibrary.repo.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.example.MangaLibrary.models.Author;
import com.example.MangaLibrary.repo.AuthorRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class AuthorController {

    @Autowired
    MangaRepo mangaRepo;
    @Autowired
    AuthorRepo authorRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    SubscriptionRepo subscriptionRepo;
    private static final int PAGE_SIZE = 10;

    @GetMapping("/author/{id}")
    public String getAuthorMangas(@PathVariable("id") long id,
                                    @RequestParam(name = "page", defaultValue = "1", required = false) int page,
                                    Model model) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending());
        Page<Manga> mangaPage = mangaRepo.findAllByAuthorIdPagination(id, pageable);
        Author author = authorRepo.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        if (author == null) {
            model.addAttribute("errorMessage", "Автора не знайдено!");
            return "main/error";
        }
        boolean isSubscribed = subscriptionRepo.existsByUserAndAuthor(user, author);
        List<Manga> mangaList = mangaPage.getContent();
        model.addAttribute("authorMangasCount", mangaPage.getTotalElements()+" Тайтла");
        model.addAttribute("page", mangaPage);
        model.addAttribute("mangas", mangaList);
        model.addAttribute("author", author);
        model.addAttribute("subscribed", isSubscribed);
        return "author/author-info";
    }

    @GetMapping("/author/add")
    public String GetAuthorAdd(Author author, Model model) throws IOException {

        model.addAttribute("author", author);
        return "author/author-add";
    }
    @PostMapping("/author/add")
    public String PostAuthorAdd(@ModelAttribute("author") @Valid Author author, BindingResult result) {
        if (result.hasErrors()) {
            return "author/author-add";
        }
        Author existingAuthor= authorRepo.findByName(author.getName());
        if (existingAuthor != null) {
            result.rejectValue("name", "error.name", "Автор з таким Ім'ям вже існує.");
            return "author/author-add";
        }

        authorRepo.save(author);
        return "redirect:/manga";
    }

    @GetMapping("/author/edit/{id}")
    public String GetAuthorEdit(@PathVariable("id") long id,Model model) throws IOException {
        Author author = authorRepo.findById(id);
        if (author == null) {
            model.addAttribute("errorMessage", "Автора не знайдено!");
            return "main/error";
        }
        model.addAttribute("author", author);
        return "author/author-edit";
    }
    @PostMapping("/author/edit/{id}")
    public String postAuthorEdit(@ModelAttribute("author") @Valid Author author, BindingResult result, @PathVariable("id") long id, Model model) {
        Author existingAuthor = authorRepo.findById(id);
        if (result.hasErrors()|| existingAuthor == null) {
            model.addAttribute("author", author);
            return "author/author-edit";
        }

        Author authorWithSameName = authorRepo.findByName(author.getName());
        if (authorWithSameName != null && authorWithSameName.getId() != id) {
            result.rejectValue("name", "error.name", "Автор з таким Ім'ям вже існує.");
            model.addAttribute("author", author);
            return "author/author-edit";
        }

        existingAuthor.setName(author.getName());
        existingAuthor.setUrlPicture(author.getUrlPicture());
        existingAuthor.setBiography(author.getBiography());

        authorRepo.save(existingAuthor);
        return "redirect:/author/"+id;
    }
    @PostMapping("/author/delete/{id}")
    public String postAuthorDelete(@PathVariable("id") long id,  Model model) {
        Author author = authorRepo.findById(id);
        if (author == null) {
            model.addAttribute("errorMessage", "Автора не знайдено!");
            return "main/error";
        }

        List<Manga> mangas = mangaRepo.findByAuthor(author);
        for (Manga manga : mangas) {
            manga.setAuthor(null);
            mangaRepo.save(manga);
        }
        authorRepo.delete(author);
        return "redirect:/manga";
    }

    @PostMapping("/author/deleteByAdminDashBoard/{id}")
    public String postAuthorDeleteByAdminDashBoard(@PathVariable("id") long id,  Model model) {
        Author author = authorRepo.findById(id);
        if (author == null) {
            model.addAttribute("errorMessage", "Автора не знайдено!");
            return "main/error";
        }

        List<Manga> mangas = mangaRepo.findByAuthor(author);
        for (Manga manga : mangas) {
            manga.setAuthor(null);
            mangaRepo.save(manga);
        }
        authorRepo.delete(author);
        return "redirect:/admin-dashboard?tab=authorsTable";
    }

    @GetMapping("/authors-filter")
    @ResponseBody
    public List<Author> getAllAuthors() {
        return authorRepo.findAll();
    }
}
