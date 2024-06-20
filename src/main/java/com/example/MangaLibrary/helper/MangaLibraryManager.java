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
    public String getResourcePathManga() {
        Path projectDir = Paths.get("").toAbsolutePath();

        Path mangasPath = Paths.get("src", "main", "resources", "static", "images", "mangas");

        Path fullPath = projectDir.resolve(mangasPath);

        return fullPath.toString();
    }
    public String getTargetPathManga() {
        String targetRootPath = new File("")
                .getAbsolutePath() + File.separator
                + "target" + File.separator + "classes" + File.separator
                + "static" + File.separator + "images" + File.separator
                + "mangas";
        return targetRootPath;
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

}