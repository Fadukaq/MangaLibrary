package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Controller
public class UserController {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MangaRepo mangaRepo;
    @GetMapping("/registration")
    public String registration(User user) {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("user") @Valid User user, BindingResult result, Map<String, Object> model, Model _model) {
        if (result.hasErrors()) {
            return "registration";
        }

        User userFromDb = userRepo.findByUserName(user.getUserName());
        if (userFromDb != null) {
            _model.addAttribute("errorMessage", "Користувач із таким ім'ям вже існує");
            return "registration";
        }
        String plainPassword = user.getUserPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        user.setUserPassword(hashedPassword);
        user.setUserRole("USER"); //USER || ADMIN
        userRepo.save(user);

        return "redirect:/login";
    }

    @GetMapping("/profile/{userName}")
    public String userProfile(@PathVariable String userName, Model model) {
        User user = userRepo.findByUserName(userName);
        if (user != null) {
            model.addAttribute("user", user);
            List<Long> readingMangaIds = user.getMangaReading().stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<Manga> readingManga = StreamSupport.stream(mangaRepo.findAllById(readingMangaIds).spliterator(), false)
                    .collect(Collectors.toList());

            List<Long> recitedMangaIds = user.getMangaRecited().stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<Manga> recitedManga = StreamSupport.stream(mangaRepo.findAllById(recitedMangaIds).spliterator(), false)
                    .collect(Collectors.toList());

            List<Long> wantToReadMangaIds = user.getMangaWantToRead().stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<Manga> wantToReadManga = StreamSupport.stream(mangaRepo.findAllById(wantToReadMangaIds).spliterator(), false)
                    .collect(Collectors.toList());

            List<Long> stoppedReadingMangaIds = user.getMangaStoppedReading().stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<Manga> stoppedReadingManga = StreamSupport.stream(mangaRepo.findAllById(stoppedReadingMangaIds).spliterator(), false)
                    .collect(Collectors.toList());



            model.addAttribute("user", user);
            model.addAttribute("readingManga", readingManga);
            model.addAttribute("recitedManga", recitedManga);
            model.addAttribute("wantToReadManga", wantToReadManga);
            model.addAttribute("stoppedReadingManga", stoppedReadingManga);
            return "user-profile";
        } else {
            model.addAttribute("errorMessage", "Пользователь с именем: " + userName + " не найден");
            return "error";
        }
    }
}