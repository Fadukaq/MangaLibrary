package com.example.MangaLibrary.helper.manga;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ChapterImage {
    private List<MultipartFile> pagesImage;
    private List<String> existingImages;
    public List<MultipartFile> getPagesImage() {
        return pagesImage;
    }

    public void setPagesImage(List<MultipartFile> pagesImage) {
        this.pagesImage = pagesImage;
    }

    public List<String> getExistingImages() {
        return existingImages;
    }

    public void setExistingImages(List<String> existingImages) {
        this.existingImages = existingImages;
    }
}
