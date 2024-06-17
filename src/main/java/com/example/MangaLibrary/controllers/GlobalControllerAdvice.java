package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Component
@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepo userRepo;
    @ModelAttribute
    public void addUserImageToModel(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepo.findByUserName(authentication.getName());
            model.addAttribute("userImageUrl", user.getProfilePicture());
        }
    }
}