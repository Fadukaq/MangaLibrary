package com.example.MangaLibrary.helper;

import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
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

@Component
public class ProjectDirectoryLocator {

    @Autowired
    private ResourceLoader resourceLoader;

    /*
     *
     * Manga Directory
     *
     * */
    public String getResourcePath() {
        Path projectDir = Paths.get("").toAbsolutePath();

        Path mangasPath = Paths.get("src", "main", "resources", "static", "images", "mangas");

        Path fullPath = projectDir.resolve(mangasPath);

        return fullPath.toString();
    }
    public String getTargetPath() {
        String targetRootPath = new File("").getAbsolutePath() + File.separator + "target" + File.separator + "classes" + File.separator + "static" + File.separator + "images" + File.separator + "mangas";
        return targetRootPath;
    }

    /*
    *
    * User Directory
    *
    * */
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