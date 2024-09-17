package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.RequestStatus;
import com.example.MangaLibrary.helper.user.UserAgreementRequest;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import com.example.MangaLibrary.service.FriendRequestService;
import com.example.MangaLibrary.service.MangaService;
import com.example.MangaLibrary.service.UserReportService;
import com.example.MangaLibrary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private MangaService mangaService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private MangaLibraryManager directoryLocator;
    @Autowired
    private UserSettingsRepo userSettingsRepo;
    @Autowired
    private NewsRepo newsRepo;
    @Autowired
    private AuthorRepo authorRepo;
    @Autowired
    private GenreRepo genreRepo;
    @Autowired
    private UserReportRepo userReportRepo;
    @Autowired
    private CommentReportRepo commentReportRepo;
    @Autowired
    private ReplyReportRepo replyReportRepo;
    @Autowired
    private UserReportService userReportService;
    private List<Manga> readingManga = new ArrayList<>();
    private List<Manga> recitedManga = new ArrayList<>();
    private List<Manga> wantToReadManga = new ArrayList<>();
    private List<Manga> favoriteManga = new ArrayList<>();
    private List<Manga> stoppedReadingManga = new ArrayList<>();
    @Autowired
    FriendRequestRepo friendRequestRepo;
    @GetMapping("/registration")
    public String registration(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "registration";
    }


    @PostMapping("/registration")
    public String addUser(@ModelAttribute("user") @Valid User user, BindingResult result, Map<String, Object> model, Model _model, RedirectAttributes redirectAttributes) {
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
        redirectAttributes.addFlashAttribute("registrationSuccessMessage", "Ви успішно зареєструвались!");
        return "redirect:/login";
    }
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        HttpServletRequest request, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
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

    @GetMapping("/profile/{id}")
    public String userProfileOId(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "15") int size,
                                 Model model) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            UserSettings userSettings = userSettingsRepo.findByUser(user);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userRepo.findByUserName(username);
            String formattedUserRole = userService.formattingUserRole(user.getUserRole());

            boolean isFriends = userRepo.getIsFriends(user, currentUser);

            if(userSettings.getProfilePrivacy().equals("private") && userSettings.getUser().getId() != currentUser.getId()) {
                model.addAttribute("user", user);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("formattedUserRole", formattedUserRole);
                model.addAttribute("profilePrivacy", "private");
                return "user/user-profile";
            }else if(userSettings.getProfilePrivacy().equals("friendly") && userSettings.getUser().getId() != currentUser.getId() && !isFriends){
                model.addAttribute("user", user);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("formattedUserRole", formattedUserRole);
                model.addAttribute("profilePrivacy", "private");
                return "user/user-profile";
            }

            Pageable pageable = PageRequest.of(page - 1, size);
            Pageable pageableComment = PageRequest.of(page - 1, 4, Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<Comment> commentsUser = userService.findCommentsByUserId(user, pageableComment);
            List<Replies> repliesUser = userService.findRepliesByUser(user);

            for (Replies reply : repliesUser) {
                Long replyUserId = userService.extractUserIdFromComment(reply.getText());
                if (replyUserId != null) {
                    Optional<User> replyUserOptional = userRepo.findById(replyUserId);
                    if (replyUserOptional.isPresent()) {
                        User replyUser = replyUserOptional.get();
                        String userName = replyUser.getUserName();

                        String updatedText = reply.getText().replaceAll("\\d+", userName);
                        reply.setText(updatedText);
                    }
                }
            }
            List<FriendRequest> friendRequests = friendRequestRepo.findByReceiverAndStatus(currentUser, RequestStatus.PENDING);
            List<FriendRequest> sentRequests = friendRequestRepo.findBySenderAndStatus(currentUser,RequestStatus.PENDING);
            List<User> userFriends = user.getFriends();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", new Locale("uk", "UA"));
            String formattedRegisterDate = user.getRegistrationDate().format(formatter);
            model.addAttribute("user", user);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("formattedRegisterDate", formattedRegisterDate);
            model.addAttribute("formattedUserRole", formattedUserRole);
            model.addAttribute("commentsUser", commentsUser);
            model.addAttribute("repliesUser", repliesUser);
            model.addAttribute("friendRequests", friendRequests);
            model.addAttribute("sentRequests", sentRequests);
            model.addAttribute("userFriends", userFriends);
            model.addAttribute("readingMangaPage", userService.getMangaPage(user.getMangaReading(), pageable));
            model.addAttribute("recitedMangaPage", userService.getMangaPage(user.getMangaRecited(), pageable));
            model.addAttribute("wantToReadMangaPage", userService.getMangaPage(user.getMangaWantToRead(), pageable));
            model.addAttribute("favoriteMangaPage", userService.getMangaPage(user.getMangaFavorites(), pageable));
            model.addAttribute("stoppedReadingMangaPage", userService.getMangaPage(user.getMangaStoppedReading(), pageable));
            return "user/user-profile";
        } else {
            model.addAttribute("errorMessage", "Користувач: " + id + " не знайдений");
            return "main/error";
        }
    }

    @GetMapping("/profile/settings/{id}")
    public String userSettings(@PathVariable("id") Long id, Model model) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserSettings userSettings = userSettingsRepo.findByUser(user);
            UserForm userForm = new UserForm();
            userForm.setUser(user);

            model.addAttribute("user", user);
            model.addAttribute("userForm", userForm);
            model.addAttribute("userSettings", userSettings);
            if (userSettings.getBackgroundImage() != null) {
                model.addAttribute("GetBackGroundImgUser", userSettings.getBackgroundImage());
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userRepo.findByUserName(username);
            if (!Objects.equals(user.getId(), currentUser.getId())) {
                model.addAttribute("errorMessage", "У вас немає доступу до налаштувань цього профілю!");
                return "main/error";
            }
            return "user/user-settings";
        }
        return "user/user-profile";
    }

    @PostMapping("/profile/settings/info/{id}")
    public String userSettingsPostInfo(@PathVariable("id") Long id,
                                       @Valid @ModelAttribute("userForm") UserForm userForm,
                                       BindingResult bindingResult,
                                       Model model, HttpSession session) {
        Optional<User> userOptional = userRepo.findById(id);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            userForm.setUser(user);
            UserSettings userSettings = userSettingsRepo.findByUser(user);
            if (bindingResult.hasErrors()) {
                model.addAttribute("user",user);
                model.addAttribute("userForm", userForm);
                model.addAttribute("userSettings", userSettings);
                return "user/user-settings";
            }
            if(userService.validateUserSettings(userForm, bindingResult)){
                userService.updateUserSettingInfo(user, userForm, userSettings);
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                if(!Objects.equals(user.getUserName(), username)){
                    session.invalidate();
                    return "redirect:/login";
                }
                return "redirect:/profile/settings/" + user.getId()+"#info";
            }else{
                model.addAttribute("user",user);
                model.addAttribute("userForm", userForm);
                model.addAttribute("userSettings", userSettings);
                return "user/user-settings";
            }
        }
        return "user/user-settings";
    }
    @PostMapping("/profile/settings/securityPassword/{id}")
    public String updateSecurityPass(@PathVariable("id") Long id,
                                     @RequestParam("userPasswordNew") String newPassword,
                                     @RequestParam("currentPassword") String currentPassword,
                                     RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            try {
                userService.isValidNewPassword(newPassword);
                userService.updatePassword(user, newPassword, currentPassword);
                redirectAttributes.addFlashAttribute("successMessagePassword", "Пароль успішно оновлено");
            }
            catch (BadCredentialsException e) {
                redirectAttributes.addFlashAttribute("errorMessagePassword", e.getMessage());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessagePassword", "Помилка при оновленні пароля");
            }
            return "redirect:/profile/settings/" + user.getId() + "#security";
        }
        return "user/user-settings";
    }

    @PostMapping("/profile/settings/securityMail/{id}")
    public ResponseEntity<String> updateSecurityMail(@PathVariable("id") Long id,
                                                     @RequestParam("newEmail") String newEmail,
                                                     @RequestParam(value = "confirmationCode", required = false) String confirmationCode,
                                                     @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                                     @RequestParam("action") String action) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isPresent()) {
            try {
                User user = userOptional.get();
                if ("getConfirmationCode".equals(action)) {
                    userService.isValidNewEmail(newEmail);
                    String generatedCode = userService.sendConfirmationCode(user, newEmail);
                    user.setEmailCode(generatedCode);
                    userRepo.save(user);
                    return ResponseEntity.ok("{\"success\":true,\"message\":\"Код підтвердження відправлено на вашу нову електронну адресу.\"}");
                } else if ("updateEmail".equals(action)) {
                    userService.isValidNewEmail(newEmail);
                    userService.updateEmail(user, newEmail, confirmationCode, currentPassword);
                    return ResponseEntity.ok("{\"success\":true,\"message\":\"Пошту успішно оновлено!\"}");
                }
            } catch (BadCredentialsException e) {
                return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"Помилка при оновленні пошти\"}");
            }
        }
        return ResponseEntity.badRequest().body("{\"success\":false,\"message\":\"Користувача не знайдено\"}");
    }


    @PostMapping("/profile/settings/background/{id}")
    public String updateBackground(@PathVariable("id") Long id,
                                   @RequestParam("backgroundImage") String selectedImage) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserSettings userSettings = userSettingsRepo.findByUser(user);
            userService.updateUserSettingsBackground(user,userSettings,selectedImage);
            return "redirect:/profile/settings/" + user.getId()+"#background";
        }
        return "user/user-settings";
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
            return "redirect:/profile/" + user.getId() + panel;
        }
        return "redirect:/error";
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
                    model.addAttribute("successPassword", "Пароль успішно скинуто.");
                    return "redirect:/login";
                } else {
                    model.addAttribute("successCode", "Код підтверджено. Введіть новий пароль.");
                }
            } else {
                model.addAttribute("errorCode", "Неправильний код скидання.");
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
    @GetMapping("/admin-dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("mangaList", mangaRepo.findAllByOrderByIdDesc());
        model.addAttribute("newsList", newsRepo.findAllByOrderByIdDesc());
        model.addAttribute("authorList", authorRepo.findAllByOrderByIdDesc());
        model.addAttribute("genreList", genreRepo.findAllByOrderByIdDesc());
        return "user/admin-dashboard";
    }
    @PostMapping("/report/user")
    @ResponseBody
    public Map<String, Object> reportUser(@RequestParam("reportedUserId") Long reportedUserId,
                                          @RequestParam("reporterUserId") Long reporterUserId,
                                          @RequestParam("reason") String reason) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> reportedUser = userRepo.findById(reportedUserId);
        Optional<User> reporterUser = userRepo.findById(reporterUserId);

        if (reportedUser.isPresent() && reporterUser.isPresent()) {
            try {
                userReportService.reportUser(reportedUser.get(), reporterUser.get(), reason);
                response.put("status", "success");
                response.put("message", "Скаргу успішно надіслано.");
            } catch (IllegalArgumentException e) {
                response.put("status", "error");
                response.put("message", "Помилка надсилання скарги");
            }
        } else {
            response.put("status", "error");
            response.put("message", "Користувача не знайдено.");
        }
        return response;
    }

     /*@GetMapping("/manga/user/{userId}/role")
    public ResponseEntity<User> getUserRole(@PathVariable Long userId) {
        Optional<User> userOptional = userRepo.findById(userId);
        if(userOptional.isPresent())
        {
            User user = userOptional.get();
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.ok(null);
    }*/
}