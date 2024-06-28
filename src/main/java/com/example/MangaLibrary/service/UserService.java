package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserSettings;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.repo.UserSettingsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.AuthenticationException;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {
    @Autowired UserRepo userRepo;
    @Autowired MangaRepo mangaRepo;
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
    private String userBackgroundImageUrl;


    public void createUser(User user){

        String plainPassword = user.getUserPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        String rootPath = directoryLocator.getResourcePathProfilePicture();
        createFolderForProfile(user, rootPath) ;
        String profilePicturePath = loadProfilePicture(null,user, rootPath);
        user.setProfilePicture(profilePicturePath);

        UserSettings userSettings = new UserSettings();
        userSettings.setBackgroundImage("/images/settingsPicture/backGroundSettings1.jpg");
        userSettings.setProfilePrivacy("public");
        userSettings.setReadStyle("left-to-right");
        userSettings.setAdultContentAgreement(false);
        user.setUserSettings(userSettings);
        userSettings.setUser(user);

        user.setUserPassword(hashedPassword);
        user.setUserRole("USER");
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
    public void getUserMangaLists(User user, List<Manga> readingManga, List<Manga> recitedManga, List<Manga> wantToReadManga, List<Manga> stoppedReadingManga) {
        readingManga.clear();
        List<Long> readingMangaIds = user.getMangaReading().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        readingManga.addAll(StreamSupport.stream(mangaRepo.findAllById(readingMangaIds).spliterator(), false)
                .toList());

        recitedManga.clear();
        List<Long> recitedMangaIds = user.getMangaRecited().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        recitedManga.addAll(StreamSupport.stream(mangaRepo.findAllById(recitedMangaIds).spliterator(), false)
                .toList());

        wantToReadManga.clear();
        List<Long> wantToReadMangaIds = user.getMangaWantToRead().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        wantToReadManga.addAll(StreamSupport.stream(mangaRepo.findAllById(wantToReadMangaIds).spliterator(), false)
                .toList());

        stoppedReadingManga.clear();
        List<Long> stoppedReadingMangaIds = user.getMangaStoppedReading().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        stoppedReadingManga.addAll(StreamSupport.stream(mangaRepo.findAllById(stoppedReadingMangaIds).spliterator(), false)
                .toList());
    }
    public User updateUserProfile(long id, UserForm userForm, String currentPassword, String userPasswordNew, boolean changePasswordCheckbox) {
        User userToUpdate = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        String realUserPassword = userToUpdate.getUserPassword();
        boolean isPasswordMatch = passwordEncoder.matches(currentPassword, realUserPassword);

        if (isPasswordMatch) {
            if (!userForm.getProfilePicture().getProfileImage().isEmpty()) {
                String rootPath = directoryLocator.getResourcePathProfilePicture();
                String userPath = loadProfilePicture(userForm.getProfilePicture().getProfileImage(), userForm.getUser(), rootPath);
                userToUpdate.setProfilePicture(userPath);
            }

            if (changePasswordCheckbox && isValidResetPass(userPasswordNew)) {
                String hashedPassword = hashPassword(userPasswordNew);
                userToUpdate.setUserPassword(hashedPassword);
            }

            userToUpdate.setAbout(userForm.getUser().getAbout());
            userRepo.save(userToUpdate);
        }
        if (!isPasswordMatch) {
            return null;
        }
        return userToUpdate;
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
        String mangaFolderName = thisUser.getUserName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
        File userFolder = new File(rootPath + File.separator + mangaFolderName);

        String targetRootPath = directoryLocator.getTargetPathProfilePicture();
        File profileFolderTarget = new File(targetRootPath + File.separator + mangaFolderName);

        if (!userFolder.exists() || !profileFolderTarget.exists()) {
            userFolder.mkdirs();
            profileFolderTarget.mkdirs();
        }
    }
    public String loadProfilePicture(MultipartFile profilePicture, User thisUser, String userFolderPath) {
        try {
            String cleanUserName = thisUser.getUserName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
            String fileName = cleanUserName + "_Profile.png";
            File targetFile = new File(userFolderPath + "/" + cleanUserName + "/" + fileName);

            if (profilePicture == null || profilePicture.isEmpty() && thisUser.getProfilePicture() == null) {
                ClassPathResource defaultImageResource = new ClassPathResource("static/images/defaultProfilePicture/defaultAvatar.png");
                File defaultImageFile = defaultImageResource.getFile();

                File userDirectory = new File(userFolderPath + "/" + cleanUserName);
                if (!userDirectory.exists()) {
                    userDirectory.mkdirs();
                }

                Files.copy(defaultImageFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                File targetClassesDirectory = new File("target/classes/static/images/profilePicture/" + cleanUserName);
                if (!targetClassesDirectory.exists()) {
                    targetClassesDirectory.mkdirs();
                }

                File targetClassesFile = new File(targetClassesDirectory + "/" + fileName);
                Files.copy(defaultImageFile.toPath(), targetClassesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                return "/images/profilePicture/" + cleanUserName + "/" + fileName;
            } else{
                byte[] bytes = profilePicture.getBytes();
                try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                    outputStream.write(bytes);
                }

                String targetRootPath =  directoryLocator.getTargetPathProfilePicture();
                File targetFolder = new File(targetRootPath + File.separator + cleanUserName);
                if (!targetFolder.exists()) {
                    targetFolder.mkdirs();
                }

                File sourceFile = new File(targetFolder + File.separator + fileName);
                try (FileOutputStream targetOutputStream = new FileOutputStream(sourceFile)) {
                    targetOutputStream.write(bytes);
                }

                File targetClassesDirectory = new File("target/classes/static/images/profilePicture/" + cleanUserName);
                if (!targetClassesDirectory.exists()) {
                    targetClassesDirectory.mkdirs();
                }

                File targetClassesFile = new File(targetClassesDirectory + "/" + fileName);
                Files.copy(targetFile.toPath(), targetClassesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                return "/images/profilePicture/" + cleanUserName + "/" + fileName;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/manga";
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
                        "Код для оновлення паролю Reset Code: %s!", user.getUserName(), resetCode
        );
        mailSender.send(user.getEmail(), "Відновлення паролю", message);
    }
    public void sendResetSuccess(User user) {

        mailSender.send(user.getEmail(), "Відновлення паролю",  "Ваш пароль було оновленно!");
    }
    public String generateResetCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public boolean validateUserSettings(UserSettings userSettings,String imgPath, BindingResult bindingResult) {
        boolean isValid = true;

        if (!isValidBackgroundImg(imgPath)) {
            bindingResult.rejectValue("backgroundImage", "error.backgroundImage",
                    "Виберіть правильне зображення і не змінюйте його розташування!");
            isValid = false;
        }

        if (!isValidProfilePrivacy(userSettings.getProfilePrivacy())) {
            bindingResult.rejectValue("profilePrivacy", "error.profilePrivacy",
                    "Виберіть правильну опцію для приватності профілю і не змінюйте внутрішні дані!");
            isValid = false;
        }

        if (!isValidReadStyles(userSettings.getReadStyle())) {
            bindingResult.rejectValue("readStyle", "error.readStyle",
                    "Виберіть правильну опцію для стилю читання і не змінюйте внутрішні дані!");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidBackgroundImg(String backGroundImg) {
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
        return privacy.equals("public") || privacy.equals("private");
    }
    private boolean isValidReadStyles(String readStyle) {
        return readStyle.equals("scroll-down") || readStyle.equals("left-to-right");
    }
    public boolean isValidResetPass(String pass) {
        if (pass == null || pass.isEmpty()) {
            return false;
        }
        int length = pass.length();
        return length >= 10 && length <= 255;
    }
}
