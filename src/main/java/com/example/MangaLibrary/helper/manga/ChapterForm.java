package com.example.MangaLibrary.helper.manga;

import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Manga;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ChapterForm {
    @Valid
    private Manga manga;
    @Valid
    private Chapter chapter;
    @Valid
    private ChapterImage chapterImage;
    private String fileOrder;

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public ChapterImage getChapterImage() {
        return chapterImage;
    }

    public void setChapterImage(ChapterImage chapterImage) {
        this.chapterImage = chapterImage;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public ChapterForm() {
        this.chapterImage = new ChapterImage();
    }

    public String getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(String fileOrder) {
        this.fileOrder = fileOrder;
    }
}
