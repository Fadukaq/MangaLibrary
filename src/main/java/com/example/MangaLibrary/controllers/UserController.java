package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.MailSender;
import com.example.MangaLibrary.services.UserService;
import com.mysql.cj.util.StringUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            model.addAttribute("message", "User successfully activated");
        }else {
            model.addAttribute("message", "Activation code is not found");
        }
        model.addAttribute("isActivated", isActivated);
        return "home";
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