package com.example.MangaLibrary.helper.manga;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public class MangaImage {
    private MultipartFile posterImage;
    private MultipartFile backGroundMangaImg;
    public MultipartFile getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(MultipartFile posterImage) {
        this.posterImage = posterImage;
    }

    public MultipartFile getBackGroundMangaImg() {
        return backGroundMangaImg;
    }

    public void setBackGroundMangaImg(MultipartFile backGroundMangaImg) {
        this.backGroundMangaImg = backGroundMangaImg;
    }
}
