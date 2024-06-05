package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.MailSender;
import com.example.MangaLibrary.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    @GetMapping("/registration")
    public String registration(User user) {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@ModelAttribute("userForm") @Valid UserForm userForm, @ModelAttribute("user") @Valid User user, BindingResult result, Map<String, Object> model, Model _model) {
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
    public String userProfile(@ModelAttribute("userForm") @Valid UserForm userForm,@PathVariable String userName, Model model) {
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