package com.example.MangaLibrary.helper;

import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.MangaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class MangaLibraryManager {

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private MangaRepo mangaRepo;
                                                            /*Manga Directory*/
    public String getResourcePath() {
        Path projectDir = Paths.get("").toAbsolutePath();

        Path mangasPath = Paths.get("src", "main", "resources", "static", "images", "mangas");

        Path fullPath = projectDir.resolve(mangasPath);

        return fullPath.toString();
    }
    public String getTargetPath() {
        String targetRootPath = new File("")
                .getAbsolutePath() + File.separator
                + "target" + File.separator + "classes" + File.separator
                + "static" + File.separator + "images" + File.separator
                + "mangas";
        return targetRootPath;
    }
    public String createFolderForManga(Manga thisManga, String rootPath) {
        String mangaFolderName = thisManga.getMangaName().replaceAll("\\s", "_")
                .replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
        File mangaFolder = new File(rootPath + File.separator + mangaFolderName);

        String targetRootPath = getTargetPath();
        File mangaFolderTarget = new File(targetRootPath + File.separator + mangaFolderName);

        if (!mangaFolder.exists() || !mangaFolderTarget.exists()) {
            mangaFolder.mkdirs();
            mangaFolderTarget.mkdirs();
        }

        return mangaFolder.getAbsolutePath();
    }

    public String createPosterManga(MultipartFile posterImg,Manga thisManga,String mangaFolderPath)
    {
        try {
            byte[] bytes = posterImg.getBytes();
            String cleanMangaName = thisManga.getMangaName()
                    .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String fileName = cleanMangaName + "_Poster.png";

            File targetFile = new File(mangaFolderPath + "/" + fileName);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            outputStream.write(bytes);
            outputStream.close();

            String targetRootPath = getTargetPath();
            File targetFolder = new File(targetRootPath + File.separator + cleanMangaName);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File sourseFile = new File(targetFolder + File.separator + fileName);
            FileOutputStream targetOutputStream = new FileOutputStream(sourseFile);
            targetOutputStream.write(bytes);
            targetOutputStream.close();

            return "/images/mangas/" + cleanMangaName + File.separator + fileName;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "redirect:/manga/add";
        }
    }

    public List<String> createPagesManga(List<MultipartFile> pagesManga, Manga thisManga, String mangaFolderPath)
            throws IOException {
        List<String> pagePaths = new ArrayList<>();
        String cleanMangaName = thisManga.getMangaName()
                .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

        File sourceFolder = new File(mangaFolderPath);
        if (!sourceFolder.exists()) {
            sourceFolder.mkdirs();
        }
        String targetRootPath = getTargetPath();
        File targetFolder = new File(targetRootPath + File.separator + cleanMangaName);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        for (int i = 0; i < pagesManga.size(); i++) {
            String fileName = cleanMangaName + "_Page" + (i + 1) + ".png";

            File sourceFile = new File(sourceFolder + File.separator + fileName);
            pagesManga.get(i).transferTo(sourceFile);

            File targetFile = new File(targetFolder + File.separator + fileName);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            pagePaths.add(fileName);
        }
        return pagePaths;
    }
    public void removeMangaFromOtherLists(User user, Long id) {
        // Удаление манги из списка mangaReading, если она там присутствует
        if (user.getMangaReading().contains(String.valueOf(id))) {
            user.getMangaReading().remove(String.valueOf(id));
        }
        if (user.getMangaStoppedReading().contains(String.valueOf(id))) {
            user.getMangaStoppedReading().remove(String.valueOf(id));
        }
        if (user.getMangaRecited().contains(String.valueOf(id))) {
            user.getMangaRecited().remove(String.valueOf(id));
        }
        if (user.getMangaWantToRead().contains(String.valueOf(id))) {
            user.getMangaWantToRead().remove(String.valueOf(id));
        }
    }
    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }
    public Long getRandomMangaId() {
        Iterable<Manga> mangas = mangaRepo.findAll();
        List<Manga> mangaList = StreamSupport.stream(mangas.spliterator(), false)
                .collect(Collectors.toList());
        if (mangaList.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(mangaList.size());
        Manga randomManga = mangaList.get(randomIndex);
        return randomManga.getId();
    }

                                                /*User Directory*/
    public String getResourcePathProfilePicture() {
        Path projectDir = Paths.get("").toAbsolutePath();

        Path mangasPath = Paths.get("src", "main", "resources", "static", "images", "profilePicture");

        Path fullPath = projectDir.resolve(mangasPath);

        return fullPath.toString();
    }
    public String getTargetPathProfilePicture() {
        String targetRootPath = new File("").getAbsolutePath() + File.separator + "target" + File.separator + "classes" + File.separator + "static" + File.separator + "images" + File.separator + "profilePicture";
        return targetRootPath;
    }
    public String createFolderForProfile(User thisUser, String rootPath) {
        String mangaFolderName = thisUser.getUserName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
        File userFolder = new File(rootPath + File.separator + mangaFolderName);

        String targetRootPath = getTargetPathProfilePicture();
        File profileFolderTarget = new File(targetRootPath + File.separator + mangaFolderName);

        if (!userFolder.exists() || !profileFolderTarget.exists()) {
            userFolder.mkdirs();
            profileFolderTarget.mkdirs();
        }
        return userFolder.getAbsolutePath();
    }
    public String loadProfilePicture(MultipartFile profilePicture, User thisUser, String userFolderPath)
    {
        try {
            String cleanUserName = thisUser.getUserName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String fileName = cleanUserName + "_Profile.png";
            File targetFile = new File(userFolderPath + "/" + cleanUserName + "/" + fileName);

            if (profilePicture == null || profilePicture.isEmpty()) {
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
            }
            else {
                byte[] bytes = profilePicture.getBytes();
                try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                    outputStream.write(bytes);
                }

                String targetRootPath = getTargetPathProfilePicture();
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
        }
        catch (IOException e) {
            e.printStackTrace();
            return "redirect:/manga";
        }
    }
}