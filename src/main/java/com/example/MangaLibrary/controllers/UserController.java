package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.UserAgreementRequest;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserSettings;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.repo.UserSettingsRepo;
import com.example.MangaLibrary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

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
    @Autowired
    private UserSettingsRepo userSettingsRepo;
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
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        HttpServletRequest request, Model model) {
        if (error != null) {
            HttpSession session = request.getSession(false);
            String errorMessage = null;
            if (session != null) {
                AuthenticationException authException = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                if (authException != null) {
                    errorMessage = userService.getErrorMessage(authException);
                }
            }
            model.addAttribute("errorMessage", errorMessage);
        }
        return "login";
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
        return "main/about";
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
            model.addAttribute("errorMessage", "Користувач: " + userName + " не знайдений");
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

        if (changePasswordCheckbox &&(!userService.isValidResetPass(userPasswordNew))) {
            model.addAttribute("changePasswordCheckbox", "on");
            model.addAttribute("errorPassword", "Поле Новий пароль повинно бути від 10 символів до 255!");
            model.addAttribute("user", userForm.getUser());
            model.addAttribute("userProfilePicture", userForm.getUser().getProfilePicture());
            model.addAttribute("userForm", userForm);
            return "user/user-edit-profile";
        }

        model.addAttribute("user", updatedUser);
        redirectAttributes.addAttribute("userName", updatedUser.getUserName());
        return "redirect:/profile/{userName}";
    }

    @GetMapping("/profile/settings/{userName}")
    public String userSettings(@PathVariable("userName") String userName ,
                                    Model model){
        User user = userRepo.findByUserName(userName);
        if(user != null){
            UserSettings userSettings = userSettingsRepo.findByUser(user);
            model.addAttribute("user",user);
            model.addAttribute("userSettings", userSettings);
            if (userSettings != null && userSettings.getBackgroundImage() != null) {
                model.addAttribute("GetBackGroundImgUser", userSettings.getBackgroundImage());
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(!Objects.equals(user.getUserName(), username)){
                model.addAttribute("errorMessage", "У вас немає доступу до налаштувань цього профілю!");
                return "main/error";
            }
            return "user/user-settings";
        }
        return "user/user-profile";
    }
    @PostMapping("/profile/settings/{userName}")
    public String userSettingsPost(@PathVariable("userName") String userName,
                                    @RequestParam("selectedImage") String selectedImage,
                                    @RequestParam(value = "adultContentAgreement", required = false) Boolean adultContentAgreement,
                                    @ModelAttribute("userSettings") UserSettings userSettings,
                                    BindingResult bindingResult,
                                    Model model) {

        User user = userRepo.findByUserName(userName);
        if (user == null) {
            return "redirect:/manga";
        }

        UserSettings existingSettings = user.getUserSettings();

        String relativeImagePath = selectedImage.substring(selectedImage.indexOf("/images"));
        if (!userService.validateUserSettings(userSettings, relativeImagePath, bindingResult)) {
            model.addAttribute("user", user);
            model.addAttribute("userSettings", userSettings);
            model.addAttribute("GetBackGroundImgUser", existingSettings.getBackgroundImage());
            model.addAttribute("selectedImage", existingSettings.getBackgroundImage());
            return "user/user-settings";
        }

        if (existingSettings != null) {
            existingSettings.setBackgroundImage(relativeImagePath);
            existingSettings.setAdultContentAgreement(adultContentAgreement != null && adultContentAgreement);
            existingSettings.setProfilePrivacy(userSettings.getProfilePrivacy());
            existingSettings.setReadStyle(userSettings.getReadStyle());
        } else {
            userSettings.setBackgroundImage(relativeImagePath);
            userSettings.setAdultContentAgreement(adultContentAgreement != null && adultContentAgreement);
            user.setUserSettings(userSettings);
            userSettings.setUser(user);
        }

        userRepo.save(user);

        return "redirect:/profile/settings/" + userName;
    }
    @PostMapping("/profile/delete-from-list/{mangaId}")
    public String deleteFromListPost(@PathVariable("mangaId") long mangaId,
                                        Principal principal,
                                        @RequestParam("panel") String panel) {
        String username = principal.getName();
        if (username!= null) {
            User user = userRepo.findByUserName(username);
            userService.deleteMangaFromUserList(user, mangaId);
            userRepo.save(user);

            return "redirect:/profile/" + username + panel;
        }
        return "redirect:/error";
    }
    @GetMapping("/admin-panel")
    public String adminPanelGet(@RequestParam(name = "username", required = false) String username, Model model) {
        List<User> users;
        if (username != null && !username.isEmpty()) {
            users = userRepo.findByUserNameContaining(username);
        } else {
            users = userRepo.findAll();
        }
        model.addAttribute("users", users);
        model.addAttribute("username", username);
        return "user/admin-panel";
    }
    @PostMapping("/admin-panel")
    public String adminPanelPost(Model model)
    {
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        return "user/admin-panel";
    }

    @PostMapping("/admin-panel/update-user")
    public String updateUser(Long userId, String role, String enabled, Model model) {
        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isPresent()) {
            User userToUpdate = optionalUser.get();
            String currentUserName = userService.getCurrentUserName();

            if (userToUpdate.getUserName().equals(currentUserName) && userToUpdate.getUserRole().equals("ADMIN")) {
                model.addAttribute("users", userRepo.findAll());
                return "redirect:/admin-panel";
            }

            Boolean enabledValue = Boolean.valueOf(enabled);

            if (!userService.isValidateRoleAndEnabled(userToUpdate, role, enabled)) {
                model.addAttribute("users", userRepo.findAll());
                return "redirect:/admin-panel";
            }

            if (role != null && userToUpdate.isEnabled()) {
                userService.updateUserRole(userId, role);
            }
            if (enabled != null) {
                userService.updateUserEnabledStatus(userId, enabledValue);
                //if(enabled.equals("false")) {
                    //userService.deleteAllCommentsByUserId(userId);
                //}
            }
        }

        model.addAttribute("users", userRepo.findAll());
        return "user/admin-panel";
    }
    @GetMapping("/reset-password")
    public String resetPasswordGet(@RequestParam(name = "email", required = false) String email,
                                    @RequestParam(name = "resetCode", required = false) String resetCode,
                                    Model model) {
        if (email != null || resetCode != null) {
            model.addAttribute("email", email);
            model.addAttribute("resetCode", resetCode);
        }
        return "user/user-reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordPost(@RequestParam("email") String email,
                                    @RequestParam(value = "resetCode", required = false) String resetCode,
                                    @RequestParam(value = "newPassword", required = false) String newPassword,
                                    @RequestParam(value = "action") String action,
                                    Model model) {
        User user = userRepo.findByEmail(email);

        if (user == null) {
            model.addAttribute("errorEmail", "Email not found.");
            return "user/user-reset-password";
        }

        if ("getResetCode".equals(action)) {
            String generatedCode = userService.generateResetCode();
            user.setResetCode(generatedCode);
            userRepo.save(user);
            userService.sendResetCode(user, user.getResetCode());
            model.addAttribute("successEmail", "Reset code sent to your email.");
        } else if ("resetPassword".equals(action)) {
            if (resetCode != null && resetCode.equals(user.getResetCode())) {
                if (newPassword != null) {
                    if (!userService.isValidResetPass(newPassword)) {
                        model.addAttribute("email", email);
                        model.addAttribute("resetCode", resetCode);
                        model.addAttribute("errorPassword", "Пароль має складатися від 10 до 255 символів і не бути порожнім.");
                        return "user/user-reset-password";
                    }
                    String hashedPass = userService.hashPassword(newPassword);
                    user.setUserPassword(hashedPass);
                    user.setResetCode(null);
                    userRepo.save(user);
                    userService.sendResetSuccess(user);
                    model.addAttribute("successPassword", "Password successfully reset.");
                    return "redirect:/login";
                } else {

                    model.addAttribute("successCode", "Code verified. Enter new password.");
                }
            } else {
                model.addAttribute("errorCode", "Invalid reset code.");
            }
        }

        model.addAttribute("email", email);
        model.addAttribute("resetCode", resetCode);
        return "user/user-reset-password";
    }

    @PostMapping("/adult-content-agreement")
    @ResponseBody
    public ResponseEntity<?> setAdultContentAgreement(@RequestBody UserAgreementRequest request, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            userService.setAdultContentAgreement(username, request.isAgreement());
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "User not authenticated"));
    }
}