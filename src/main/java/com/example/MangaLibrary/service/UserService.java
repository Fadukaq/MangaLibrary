package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.user.UserForm;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {
    @Autowired UserRepo userRepo;
    @Autowired MangaRepo mangaRepo;
    @Autowired
    private MangaRepo mangaRepository;
    @Autowired
    private MangaLibraryManager directoryLocator;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public void createUser(User user){

        String plainPassword = user.getUserPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        String rootPath = directoryLocator.getResourcePathProfilePicture();
        directoryLocator.createFolderForProfile(user, rootPath) ;
        String profilePicturePath = directoryLocator.loadProfilePicture(null,user, rootPath);
        user.setProfilePicture(profilePicturePath);

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
                String userPath = directoryLocator.loadProfilePicture(userForm.getProfilePicture().getProfileImage(), userForm.getUser(), rootPath);
                userToUpdate.setProfilePicture(userPath);
            }

            if (changePasswordCheckbox && userPasswordNew != null && userPasswordNew.length() >= 2 && userPasswordNew.length() <= 255) {
                String hashedPassword = passwordEncoder.encode(userPasswordNew);
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

}
