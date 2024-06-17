package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class UserController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private MangaLibraryManager directoryLocator;
    private List<Manga> readingManga = new ArrayList<>();
    private List<Manga> recitedManga = new ArrayList<>();
    private List<Manga> wantToReadManga = new ArrayList<>();
    private List<Manga> stoppedReadingManga = new ArrayList<>();
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
        User mailFromDb = userRepo.findByEmail(user.getEmail());
        if (userFromDb != null) {
            _model.addAttribute("errorMessage", "Користувач із таким ім'ям вже існує");
            return "registration";
        }
        if(mailFromDb!= null){
            _model.addAttribute("errorMessage", "Користувач із такою поштою вже існує");
            return "registration";
        }

        userService.createUser(user);
        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code){
        boolean isActivated = userService.activateUser(code);
        if(isActivated){
            model.addAttribute("message", "Користувача успішно активовано!");
        }else {
            model.addAttribute("message", "Код активації не знайдено.");
        }
        model.addAttribute("isActivated", isActivated);
        return "main/home";
    }

    @GetMapping("/profile/{userName}")
    public String userProfile(@PathVariable String userName, Model model) {
        User user = userRepo.findByUserName(userName);
        if (user != null) {
            model.addAttribute("user", user);
            userService.getUserMangaLists(user, readingManga, recitedManga, wantToReadManga, stoppedReadingManga);

            model.addAttribute("readingManga", readingManga);
            model.addAttribute("recitedManga", recitedManga);
            model.addAttribute("wantToReadManga", wantToReadManga);
            model.addAttribute("stoppedReadingManga", stoppedReadingManga);
            return "user/user-profile";
        } else {
            model.addAttribute("errorMessage", "Пользователь с именем: " + userName + " не найден");
            return "main/error";
        }
    }

    @GetMapping("/profile/edit/{id}")
    public String userEditProfile(@PathVariable("id") long id ,
                                    Model model){
        Optional<User> userOptional = userRepo.findById(id);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            UserForm userForm = new UserForm();
            userForm.setUser(user);

            model.addAttribute("user",user);
            model.addAttribute("userForm", userForm);
            model.addAttribute("userProfilePicture",user.getProfilePicture());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(!Objects.equals(user.getUserName(), username)){
                model.addAttribute("errorMessage", "У вас немає доступу змінювати цей профіль!");
                return "main/error";
            }

            return "user/user-edit-profile";
        }
        return "user/user-profile";
    }

    @PostMapping("/profile/edit/{id}")
    public String userEditProfilePost(@PathVariable("id") long id, @Valid @ModelAttribute("userForm") UserForm userForm,
                                        BindingResult bindingResult,
                                        @RequestParam String currentPassword,
                                        @RequestParam(name = "userPasswordNew", required = false) String userPasswordNew,
                                        @RequestParam(name = "changePasswordCheckbox", required = false) boolean changePasswordCheckbox,
                                        RedirectAttributes redirectAttributes,
                                        Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userForm.getUser());
            model.addAttribute("userProfilePicture", userForm.getUser().getProfilePicture());
            model.addAttribute("userForm", userForm);
            model.addAttribute("validationErrors", bindingResult.getAllErrors());
            return "user/user-edit-profile";
        }

        User updatedUser = userService.updateUserProfile(id, userForm, currentPassword, userPasswordNew, changePasswordCheckbox);

        if (updatedUser == null) {
            model.addAttribute("errorMessage", "Обов'язково введіть правильний пароль!");
            model.addAttribute("user", userForm.getUser());
            model.addAttribute("userProfilePicture", userForm.getUser().getProfilePicture());
            model.addAttribute("userForm", userForm);
            return "user/user-edit-profile";
        }

        if (changePasswordCheckbox && userPasswordNew != null && (userPasswordNew.length() < 2 || userPasswordNew.length() > 255)) {
            model.addAttribute("changePasswordCheckbox", "on");
            model.addAttribute("errorPassword", "Поле Новий пароль повинно бути від 2 символів до 255!");
            model.addAttribute("user", userForm.getUser());
            model.addAttribute("userProfilePicture", userForm.getUser().getProfilePicture());
            model.addAttribute("userForm", userForm);
            return "user/user-edit-profile";
        }

        model.addAttribute("user", updatedUser);
        redirectAttributes.addAttribute("userName", updatedUser.getUserName());
        return "redirect:/profile/{userName}";
    }
    @PostMapping("/profile/delete-from-list/{mangaId}")
    public String deleteFromListPost(@PathVariable("mangaId") long mangaId, Principal principal, Model model){
        String username = principal.getName();
        if(username != null){
            User user = userRepo.findByUserName(username);
            userService.deleteMangaFromUserList(user, mangaId);
            userRepo.save(user);
        }
        return "redirect:/profile/" + username;
    }
    @GetMapping("/admin-panel")
    public String adminPanelGet(Model model)
    {
        return "user/admin-panel";
    }
    @PostMapping("/admin-panel")
    public String adminPanelPost(Model model)
    {
        return "user/admin-panel";
    }
}