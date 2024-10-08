package com.example.MangaLibrary.helper.manga;

import com.example.MangaLibrary.models.Author;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public class MangaForm {
    @Valid
    private Manga manga;
    @Valid
    private MangaImage mangaImage;
    private MangaImage backGroundImg;
    @Valid
    private List<Genre> genres = new ArrayList<>();
    @Valid
    private Author author;
    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public MangaImage getMangaImage() {
        return mangaImage;
    }

    public void setMangaImage(MangaImage mangaImage) {
        this.mangaImage = mangaImage;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public MangaImage getBackGroundImg() {
        return backGroundImg;
    }

    public void setBackGroundImg(MangaImage backGroundImg) {
        this.backGroundImg = backGroundImg;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}