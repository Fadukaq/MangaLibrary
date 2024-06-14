package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.config.SecurityConfig;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.MailSender;
import com.example.MangaLibrary.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private MailSender mailSender;
    @Autowired
    private MangaLibraryManager directoryLocator;
    @Autowired
    private SecurityConfig securityConfig;

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

        String plainPassword = user.getUserPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        String rootPath = directoryLocator.getResourcePathProfilePicture();
        directoryLocator.createFolderForProfile(user, rootPath) ;
        String profilePicturePath = directoryLocator.loadProfilePicture(null,user, rootPath);
        user.setProfilePicture(profilePicturePath);

        user.setUserPassword(hashedPassword);
        user.setUserRole("USER"); //USER || ADMIN
        user.setEnabled(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepo.save(user);
        if(!user.getEmail().isEmpty()){
            String message = String.format(
                    "Привіт, %s! \n"+
                            "Ласкаво просимо до MangaLibrary. Будь ласка, перейдіть за цим посиланням, щоб активувати акаунт: http://localhost:8080/activate/%s", user.getUserName(),user.getVerificationToken()
            );
            mailSender.send(user.getEmail(), "Активація облікового запису", message);
        }
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
        return "home";
    } //rename attributeValue
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
                return "error";
            }

            return "user-edit-profile";
        }
        return "user-profile";
    }
    @PostMapping("/profile/edit/{id}")
    public String userEditProfilePost(@PathVariable("id") long id, @Valid @ModelAttribute("userForm") UserForm userForm,
                                        BindingResult bindingResult,
                                        @RequestParam String currentPassword,
                                        @RequestParam(name = "userPasswordNew", required = false) String userPasswordNew,
                                        @RequestParam(name = "changePasswordCheckbox", required = false) boolean changePasswordCheckbox,
                                        RedirectAttributes redirectAttributes,
                                        Model model){
            if(bindingResult.hasErrors()){
                model.addAttribute("user",userForm.getUser());
                model.addAttribute("userProfilePicture",userForm.getUser().getProfilePicture());
                model.addAttribute("userForm", userForm);
                model.addAttribute("validationErrors", bindingResult.getAllErrors());
                return "user-edit-profile";
            }
            User userToUpdate = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
            String realUserPassword = userToUpdate.getUserPassword();
            boolean isPasswordMatch = passwordEncoder.matches(currentPassword, realUserPassword);

            if(isPasswordMatch){
                if(!userForm.getProfilePicture().getProfileImage().isEmpty()){
                    String rootPath = directoryLocator.getResourcePathProfilePicture();
                    String userPath = directoryLocator.loadProfilePicture(userForm.getProfilePicture().getProfileImage(), userForm.getUser(), rootPath);
                    userToUpdate.setProfilePicture(userPath);
                }
                if (changePasswordCheckbox && userPasswordNew != null && userPasswordNew.length() >= 2 && userPasswordNew.length() <= 255) {
                    String hashedPassword = passwordEncoder.encode(userPasswordNew);
                    userToUpdate.setUserPassword(hashedPassword);
                }else if (changePasswordCheckbox) {
                    model.addAttribute("changePasswordCheckbox", "on");

                    model.addAttribute("errorPassword", "Поле Новий пароль повинно бути від 2 символів до 255!");
                    model.addAttribute("user", userForm.getUser());
                    model.addAttribute("userProfilePicture", userForm.getUser().getProfilePicture());
                    model.addAttribute("userForm", userForm);
                    return "user-edit-profile";
                }

                userToUpdate.setAbout(userForm.getUser().getAbout());

                model.addAttribute("user",userForm.getUser());
                redirectAttributes.addAttribute("userName", userForm.getUser().getUserName());
                userRepo.save(userToUpdate);
                return "redirect:/profile/{userName}";
            }else{
                model.addAttribute("errorMessage", "Обов'язково введіть правильний пароль!");
            }
            model.addAttribute("user",userForm.getUser());
            model.addAttribute("userProfilePicture",userForm.getUser().getProfilePicture());
            model.addAttribute("userForm", userForm); // Передаем объект UserForm в модель
        return "user-edit-profile";
    }
    @GetMapping("/admin-panel")
    public String adminPanelGet(Model model)
    {
        return "admin-panel";
    }
    @PostMapping("/admin-panel")
    public String adminPanelPost(Model model)
    {
        return "admin-panel";
    }
}