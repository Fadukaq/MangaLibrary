package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserSettings;
import com.example.MangaLibrary.repo.*;
import com.example.MangaLibrary.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    public void addBackGroundUser(Authentication authentication, Model model,
                                  HttpSession session) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepo.findByUserName(authentication.getName());
            if (user != null) {
                String username = authentication.getName();
                if(!user.isEnabled()){
                    if(Objects.equals(user.getUserName(), username)){
                        session.invalidate();
                    }
                }
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if(user.getUserRole() != auth.getPrincipal()) {
                    SimpleGrantedAuthority updatedAuthority = new SimpleGrantedAuthority(user.getUserRole());
                    Authentication newAuth = new UsernamePasswordAuthenticationToken(
                            auth.getPrincipal(),
                            auth.getCredentials(),
                            Collections.singletonList(updatedAuthority)
                    );
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
                model.addAttribute("userImageUrl", user.getProfilePicture());
                model.addAttribute("userId", user.getId());
                UserSettings userSettingsOptional = userSettingsRepo.findByUser(user);
                model.addAttribute("backGroundImgUser", userSettingsOptional.getBackgroundImage());
            }
        }
    }
}