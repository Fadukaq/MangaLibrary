package com.example.MangaLibrary.helper.manga;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public class MangaImage {
    private MultipartFile posterImage;
    private MultipartFile backGroundMangaImg;
    private List<MultipartFile> pagesImage;
    public MultipartFile getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(MultipartFile posterImage) {
        this.posterImage = posterImage;
    }

    public List<MultipartFile> getPagesImage() {
        return pagesImage;
    }

    public void setPagesImage(List<MultipartFile> pagesImage) {
        this.pagesImage = pagesImage;
    }

    public MultipartFile getBackGroundMangaImg() {
        return backGroundMangaImg;
    }

    public void setBackGroundMangaImg(MultipartFile backGroundMangaImg) {
        this.backGroundMangaImg = backGroundMangaImg;
    }
}
