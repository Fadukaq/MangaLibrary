package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Author;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.Subscription;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.AuthorRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.SubscriptionRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class SubscriptionController {
    @Autowired
    UserService userService;
    @Autowired
    UserRepo userRepo;
    @Autowired
    AuthorRepo authorRepo;
    @Autowired
    MangaRepo mangaRepo;
    @Autowired
    SubscriptionRepo subscriptionRepo;

    @PostMapping("/subscribe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSubscription(@RequestParam(required = false) Long mangaId,
                                                                  @RequestParam(required = false) Long authorId,
                                                                  Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepo.findByUserName(principal.getName());

            if (mangaId != null) {
                Optional<Manga> manga = mangaRepo.findById(mangaId);
                if (manga.isPresent()) {
                    Subscription existingSubscription = subscriptionRepo.findByUserAndManga(user, manga.get());

                    if (existingSubscription != null) {
                        subscriptionRepo.delete(existingSubscription);
                        response.put("subscribed", false);
                    } else {
                        Subscription newSubscription = new Subscription();
                        newSubscription.setUser(user);
                        newSubscription.setManga(manga.get());
                        newSubscription.setSubscriptionDate(LocalDateTime.now());
                        subscriptionRepo.save(newSubscription);
                        response.put("subscribed", true);
                    }
                    return ResponseEntity.ok(response);
                }
            }

            if (authorId != null) {
                Optional<Author> author = authorRepo.findById(authorId);
                if (author.isPresent()) {
                    Subscription existingSubscription = subscriptionRepo.findByUserAndAuthor(user, author.get());

                    if (existingSubscription != null) {
                        subscriptionRepo.delete(existingSubscription);
                        response.put("subscribed", false);
                    } else {
                        Subscription newSubscription = new Subscription();
                        newSubscription.setUser(user);
                        newSubscription.setAuthor(author.get());
                        newSubscription.setSubscriptionDate(LocalDateTime.now());
                        subscriptionRepo.save(newSubscription);
                        response.put("subscribed", true);
                    }
                    return ResponseEntity.ok(response);
                }
            }

            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
        @GetMapping("/check-subscription")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> checkSubscription(@RequestParam Long mangaId, Principal principal) {
            Map<String, Object> response = new HashMap<>();

            try {
                User user = userRepo.findByUserName(principal.getName());
                Optional<Manga> manga = mangaRepo.findById(mangaId);

                if (manga.isPresent()) {
                    Subscription existingSubscription = subscriptionRepo.findByUserAndManga(user, manga.get());

                    if (existingSubscription != null) {
                        response.put("subscribed", true);
                    } else {
                        response.put("subscribed", false);
                    }

                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

            } catch (Exception e) {
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
}
