package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {
    @Autowired UserRepo userRepo;
    @Autowired MangaRepo mangaRepo;
    @Autowired
    CommentRepo commentRepo;
    @Autowired
    RepliesRepo replyRepo;
    @Autowired
    private UserSettingsRepo userSettingsRepo;
    @Autowired
    private MangaRepo mangaRepository;
    @Autowired
    private MangaLibraryManager directoryLocator;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    HttpSession session;
    public void createUser(User user){
        String plainPassword = user.getUserPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        userRepo.save(user);
        String rootPath = directoryLocator.getResourcePathProfilePicture();
        createFolderForProfile(user, rootPath) ;
        String profilePicturePath = loadProfilePicture(null,user, rootPath);
        user.setProfilePicture(profilePicturePath);

        UserSettings userSettings = new UserSettings();
        userSettings.setBackgroundImage("/images/settingsPicture/backGroundSettings1.jpg");
        userSettings.setProfilePrivacy("public");
        userSettings.setReadStyle("left-to-right");
        userSettings.setPageStyle("book-view");
        userSettings.setAdultContentAgreement(false);
        user.setUserSettings(userSettings);
        userSettings.setUser(user);

        user.setUserPassword(hashedPassword);
        user.setUserRole("USER");
        user.setRegistrationDate(LocalDateTime.now());
        user.setEnabled(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepo.save(user);
        if(!user.getEmail().isEmpty()){
            String message = String.format(
                    "Привіт, %s! \n"+
                            "Ласкаво просимо до MangaLibrary. Будь ласка, перейдіть за цим посиланням, щоб активувати акаунт: http://mangalibrary-production.up.railway.app/activate/%s", user.getUserName(),user.getVerificationToken()
            );
            mailSender.send(user.getEmail(), "Активація облікового запису", message);
        }
    }
    public boolean activateUser(String code) {
        User user = userRepo.findByVerificationToken(code);
        if(user == null){
            return false;
        }
        user.setVerificationToken(null);
        user.setEnabled(true);
        userRepo.save(user);
        return true;
    }
    public Page<Manga> getMangaPage(List<String> mangaIds, Pageable pageable) {
        List<Long> ids = mangaIds.stream().map(Long::valueOf).collect(Collectors.toList());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("averageRating").descending());
        return mangaRepo.findAllByIdIn(ids, sortedPageable);
    }

    public String hashPassword(String pass){
        return passwordEncoder.encode(pass);
    }
    public void deleteMangaFromUserList(User user, Long mangaId) {
        Manga manga = mangaRepository.findById(mangaId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id: " + mangaId));
        user.getMangaReading().removeIf(id -> id.equals(String.valueOf(manga.getId())));
        user.getMangaWantToRead().removeIf(id -> id.equals(String.valueOf(manga.getId())));
        user.getMangaStoppedReading().removeIf(id -> id.equals(String.valueOf(manga.getId())));
        user.getMangaFavorites().removeIf(id -> id.equals(String.valueOf(manga.getId())));
        user.getMangaRecited().removeIf(id -> id.equals(String.valueOf(manga.getId())));
    }
    public void deleteMangaFromUsersList(List<User> users, long mangaId){
        for (User user : users) {
            user.getMangaReading().removeIf(id -> id.equals(String.valueOf(mangaId)));
            user.getMangaWantToRead().removeIf(id -> id.equals(String.valueOf(mangaId)));
            user.getMangaStoppedReading().removeIf(id -> id.equals(String.valueOf(mangaId)));
            user.getMangaRecited().removeIf(id -> id.equals(String.valueOf(mangaId)));
            userRepo.save(user);
        }
    }
    public String getErrorMessage(AuthenticationException exception) {
        String message = exception.getMessage();
        if (message.equalsIgnoreCase("User is disabled")) {
            return "Ваш акаунт не активований.";
        }
        else {
            return "Невірне ім'я користувача або пароль.";
        }
    }

    public void addMangaToList(User user, String listType, Long mangaId) {
        switch (listType) {
            case "reading":
                user.getMangaReading().add(String.valueOf(mangaId));
                break;
            case "want-read":
                user.getMangaWantToRead().add(String.valueOf(mangaId));
                break;
            case "recited":
                user.getMangaRecited().add(String.valueOf(mangaId));
                break;
            case "read-stopped":
                user.getMangaStoppedReading().add(String.valueOf(mangaId));
                break;
            default:
                break;
        }
    }
    public void createFolderForProfile(User thisUser, String rootPath) {
        File userFolder = new File(rootPath + File.separator + thisUser.getId());

        String targetRootPath = directoryLocator.getTargetPathProfilePicture();
        File profileFolderTarget = new File(targetRootPath + File.separator + thisUser.getId());

        if (!userFolder.exists() || !profileFolderTarget.exists()) {
            userFolder.mkdirs();
            profileFolderTarget.mkdirs();
        }
    }

    public String loadProfilePicture(MultipartFile profilePicture, User thisUser, String userFolderPath) {
        try {
            String fileName = thisUser.getId() + "_Profile.png";
            File targetFile = new File(userFolderPath + "/" + thisUser.getId() + "/" + fileName);

            if (profilePicture == null || profilePicture.isEmpty() && thisUser.getProfilePicture() == null) {
                ClassPathResource defaultImageResource = new ClassPathResource("static/images/defaultProfilePicture/defaultAvatar.png");
                File defaultImageFile = defaultImageResource.getFile();

                File userDirectory = new File(userFolderPath + "/" + thisUser.getId());
                if (!userDirectory.exists()) {
                    userDirectory.mkdirs();
                }

                Files.copy(defaultImageFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                File targetClassesDirectory = new File("target/classes/static/images/profilePicture/" + thisUser.getId());
                if (!targetClassesDirectory.exists()) {
                    targetClassesDirectory.mkdirs();
                }

                File targetClassesFile = new File(targetClassesDirectory + "/" + fileName);
                Files.copy(defaultImageFile.toPath(), targetClassesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                return "/images/profilePicture/" + thisUser.getId() + "/" + fileName;
            } else{
                byte[] bytes = profilePicture.getBytes();
                try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                    outputStream.write(bytes);
                }

                String targetRootPath =  directoryLocator.getTargetPathProfilePicture();
                File targetFolder = new File(targetRootPath + File.separator + thisUser.getId());
                if (!targetFolder.exists()) {
                    targetFolder.mkdirs();
                }

                File sourceFile = new File(targetFolder + File.separator + fileName);
                try (FileOutputStream targetOutputStream = new FileOutputStream(sourceFile)) {
                    targetOutputStream.write(bytes);
                }

                File targetClassesDirectory = new File("target/classes/static/images/profilePicture/" + thisUser.getId());
                if (!targetClassesDirectory.exists()) {
                    targetClassesDirectory.mkdirs();
                }

                File targetClassesFile = new File(targetClassesDirectory + "/" + fileName);
                Files.copy(targetFile.toPath(), targetClassesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                return "/images/profilePicture/" + thisUser.getId() + "/" + fileName;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/images/defaultProfilePicture/defaultAvatar.png";
        }
    }

    public void setAdultContentAgreement(String username, boolean agreement) {
        User user = userRepo.findByUserName(username);
        UserSettings userSettings = userSettingsRepo.findByUser(user);
        userSettings.setAdultContentAgreement(agreement);
        userSettingsRepo.save(userSettings);
    }

    public void sendResetCode(User user, String resetCode) {
        String message = String.format(
                "Вітаю, %s! \n"+
                        "Код для оновлення паролю: %s!", user.getUserName(), resetCode
        );
        mailSender.send(user.getEmail(), "Відновлення паролю", message);
    }
    public void sendResetSuccess(User user) {

        mailSender.send(user.getEmail(), "Відновлення паролю",  "Ваш пароль було оновленно!");
    }
    public String generateResetCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public boolean validateUserSettings(UserForm userForm, BindingResult bindingResult) {
        boolean isValid = true;

        if (!isValidProfilePrivacy(userForm.getUserSettings().getProfilePrivacy())) {
            bindingResult.rejectValue("userSettings.profilePrivacy", "error.profilePrivacy",
                    "Виберіть правильну опцію для приватності профілю і не змінюйте внутрішні дані!");
            isValid = false;
        }

        if (!isValidReadStyles(userForm.getUserSettings().getReadStyle())) {
            bindingResult.rejectValue("userSettings.readStyle", "error.readStyle",
                    "Виберіть правильну опцію для стилю читання і не змінюйте внутрішні дані!");
            isValid = false;
        }
        if (!isValidPageStyles(userForm.getUserSettings().getPageStyle())) {
            bindingResult.rejectValue("userSettings.pageStyle", "error.pageStyle",
                    "Виберіть правильну опцію для стилю сторінки і не змінюйте внутрішні дані!");
            isValid = false;
        }
        return isValid;
    }

    public boolean isValidBackgroundImg(String backGroundImg) {
        if (backGroundImg == null) {
            return false;
        }
        return
                backGroundImg.equals("/images/settingsPicture/backGroundSettings1.jpg") ||
                backGroundImg.equals("/images/settingsPicture/backGroundSettings2.jpg") ||
                backGroundImg.equals("/images/settingsPicture/backGroundSettings3.jpg") ||
                backGroundImg.equals("/images/settingsPicture/backGroundSettings4.jpg") ||
                backGroundImg.equals("/images/settingsPicture/backGroundSettings5.jpg") ||
                backGroundImg.equals("/images/settingsPicture/backGroundSettings6.jpg");
    }
    private boolean isValidProfilePrivacy(String privacy) {
        return privacy.equals("public") || privacy.equals("private") || privacy.equals("friendly");
    }
    public boolean isValidReadStyles(String readStyle) {
        return readStyle.equals("scroll-down") || readStyle.equals("left-to-right");
    }
    public boolean isValidPageStyles(String pageStyle) {
        return pageStyle.equals("book-view") || pageStyle.equals("single-page-view");
    }
    public boolean isValidResetPass(String pass) {
        if (pass == null || pass.isEmpty()) {
            return false;
        }
        int length = pass.length();
        return length >= 10 && length <= 255;
    }

    public String getCurrentUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        Optional<User> user = userRepo.findById(userId);
        if(user.isPresent()) {
            User useToUpdate = user.get();
            useToUpdate.setUserRole(role);
            userRepo.save(useToUpdate);


        }
    }

    @Transactional
    public void updateUserEnabledStatus(Long userId, Boolean enabled) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent()) {
            User userToUpdate = user.get();
            userToUpdate.setEnabled(enabled);
            userRepo.save(userToUpdate);
        }
    }

    public boolean isValidateRoleAndEnabled(User user, String role, String enabled) {
        if (role != null && (role.equals("ADMIN") || role.equals("USER") || role.equals("MODERATOR"))) {
            return true;
        }
        if (enabled != null && (enabled.equals("true") || enabled.equals("false")) && !"ADMIN".equals(user.getUserRole())) {
            return true;
        }
        return false;
    }
    public Page<Comment> findCommentsByUserId(User user, Pageable pageable) {
        return commentRepo.findByUser(user, pageable);
    }

    public List<Replies> findRepliesByUser(User user) {
        return replyRepo.findByUser(user);
    }
    public Long extractUserIdFromComment(String commentText) {
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = pattern.matcher(commentText);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
    public User extractUserFromComment(String commentText) {
        Pattern pattern = Pattern.compile("@([A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(commentText);
        if (matcher.find()) {
            String username = matcher.group(1);
            return userRepo.findByUserName(username);
        }
        return null;
    }
    public String formattingUserRole(String userRole) {
        return switch (userRole.toUpperCase()) {
            case "ADMIN" -> "Адміністратор";
            case "MODERATOR" -> "Модератор";
            case "AUTHOR" -> "Автор";
            case "USER" -> "Користувач";
            default -> userRole;
        };
    }
    public String getUsernameById(long userId) {
        User user = userRepo.findById(userId).orElse(null);
        return user != null ? user.getUserName() : "Unknown";
    }

    public void updateUserSettingInfo(User userToUpdate, UserForm userForm, UserSettings userSettings) {
        User existingUser = userRepo.findByUserName(userForm.getUser().getUserName());
        if (existingUser != null && existingUser.getId() != userToUpdate.getId()) {
            throw new IllegalArgumentException("Нік вже зайнятий іншим користувачем");
        }

        if (userForm.getProfilePicture().getProfileImage() != null && !userForm.getProfilePicture().getProfileImage().isEmpty()) {
            String rootPath = directoryLocator.getResourcePathProfilePicture();
            String userPath = loadProfilePicture(userForm.getProfilePicture().getProfileImage(), userForm.getUser(), rootPath);
            userToUpdate.setProfilePicture(userPath);
        }

        userToUpdate.setUserName(userForm.getUser().getUserName());
        userToUpdate.setAbout(userForm.getUser().getAbout());
        if(userForm.getUserSettings().getAdultContentAgreement()==null){
            userSettings.setAdultContentAgreement(false);
        }else{
            userSettings.setAdultContentAgreement(userForm.getUserSettings().getAdultContentAgreement());
        }
        userSettings.setProfilePrivacy(userForm.getUserSettings().getProfilePrivacy());
        userSettings.setReadStyle(userForm.getUserSettings().getReadStyle());
        userSettings.setPageStyle(userForm.getUserSettings().getPageStyle());

        userRepo.save(userToUpdate);
    }
    public void updateUserSettingsBackground(User user, UserSettings userSettings, String selectedImage){
        String relativeImagePath = selectedImage.substring(selectedImage.indexOf("/images"));
        if(isValidBackgroundImg(relativeImagePath)){
            userSettings.setBackgroundImage(relativeImagePath);
            userSettingsRepo.save(userSettings);
            user.setUserSettings(userSettings);
            userRepo.save(user);
        }
    }

    public void updateEmail(User user, String newEmail, String confirmationCode, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            throw new BadCredentialsException("Невірний поточний пароль");
        }

        if (!confirmationCode.equals(user.getEmailCode())) {
            throw new BadCredentialsException("Невірний код підтвердження");
        }

        user.setEmail(newEmail);
        user.setEmailCode(null);
        userRepo.save(user);
    }
    public void updatePassword(User user, String newPassword, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            throw new BadCredentialsException("Невірний поточний пароль");
        }
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
    public String sendConfirmationCode(User user, String newEmail) {
        String confirmationCode = generateResetCode();
        user.setEmailCode(confirmationCode);
        userRepo.save(user);
        mailSender.send(newEmail, "Код підтвердження для зміни email", "Ваш код підтвердження: " + confirmationCode);
        return (confirmationCode);
    }


    public boolean isValidNewPassword(String newPassword) {
        if (newPassword.length() < 10 || newPassword.length() > 255) {
            throw new BadCredentialsException("Новий пароль має складатись від 10 до 255 символів.");
        }
        return true;
    }

    public boolean isValidNewEmail(String newEmail) {
        if (newEmail == null || newEmail.isEmpty()) {
            throw new BadCredentialsException("Нова пошта не може бути пустою");
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!newEmail.matches(emailRegex)) {
            throw new BadCredentialsException("Невірний формат нової пошти");
        }
        if (userRepo.existsByEmail(newEmail)) {
            throw new BadCredentialsException("Ця пошта вже використовується");
        }
        return true;
    }
}
