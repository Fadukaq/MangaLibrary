package com.example.MangaLibrary.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
public class Manga {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotEmpty(message = "Назва манги не повинно бути порожнім")
    @Size(min=5,max=512, message = "Назва манги має складатися від 5 до 512 символів")
    private String mangaName;

    @NotEmpty(message = "Опис манги не повинно бути порожнім")
    @Size(min=10,max=2048, message = "Опис манги має складатися від 10 до 2048 символів")
    String mangaDescription;

    @ManyToMany
    @JoinTable(
            name = "manga_genre",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
        private List<Genre> genres;

    @NotEmpty(message = "Автор манги не повинно бути порожнім")
    @Size(min=5,max=256, message = "Автор манги має складатися від 5 до 256 символів")
    String mangaAuthor;

    String mangaPosterImg;

    String mangaPages;
    String mangaBackGround;

    @NotEmpty(message = "Рік релізу не повинно бути порожнім")
    private String releaseYear;

    public Manga() {}
    public Manga(Manga manga) {
        this.mangaName = manga.getMangaName();
        this.mangaDescription = manga.getMangaDescription();
        this.mangaAuthor = manga.getMangaAuthor();
        this.mangaPosterImg = manga.getMangaPosterImg();
        this.mangaPages = manga.getMangaPages();
        this.releaseYear = manga.getReleaseYear();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMangaName() {
        return mangaName;
    }

    public void setMangaName(String mangaName) {
        this.mangaName = mangaName;
    }

    public String getMangaDescription() {
        return mangaDescription;
    }

    public void setMangaDescription(String mangaDescription) {
        this.mangaDescription = mangaDescription;
    }

    public String getMangaAuthor() {
        return mangaAuthor;
    }

    public void setMangaAuthor(String mangaAuthor) {
        this.mangaAuthor = mangaAuthor;
    }

    public String getMangaPosterImg() {
        return mangaPosterImg;
    }

    public void setMangaPosterImg(String mangaPosterImg) {
        this.mangaPosterImg = mangaPosterImg;
    }

    public String getMangaPages() {
        return mangaPages;
    }

    public void setMangaPages(String mangaPages) {
        this.mangaPages = mangaPages;
    }


    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public String getMangaBackGround() {
        return mangaBackGround;
    }

    public void setMangaBackGround(String mangaBackGround) {
        this.mangaBackGround = mangaBackGround;
    }
}
