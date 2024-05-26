package com.example.MangaLibrary.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ProjectDirectoryLocator {

    @Autowired
    private ResourceLoader resourceLoader;

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

}