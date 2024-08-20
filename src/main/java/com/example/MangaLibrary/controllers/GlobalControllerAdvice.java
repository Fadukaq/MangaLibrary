package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserSettings;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.repo.UserSettingsRepo;
import com.example.MangaLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Component
@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private UserSettingsRepo userSettingsRepo;
    @ModelAttribute
    public void addBackGroundUser(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepo.findByUserName(authentication.getName());
            if (user != null) {
                model.addAttribute("userImageUrl", user.getProfilePicture());
                model.addAttribute("userId", user.getId());
                UserSettings userSettingsOptional = userSettingsRepo.findByUser(user);
                model.addAttribute("backGroundImgUser", userSettingsOptional.getBackgroundImage());
            }
        }
    }
}