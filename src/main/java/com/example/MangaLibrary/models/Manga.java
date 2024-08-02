package com.example.MangaLibrary.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
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
    String mangaStatus;

    @ManyToMany
    @JoinTable(
            name = "manga_genre",
            joinColumns = @JoinColumn(name = "manga_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
        private List<Genre> genres;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    String mangaPosterImg;

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<Chapter> chapter = new ArrayList<>();

    String mangaBackGround;
    @NotEmpty(message = "Рік релізу не повинно бути порожнім")
    private String releaseYear;

    private Boolean adultContent;

    private double averageRating;
    private int totalRatings;
    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();
    public Manga() {}
    public Manga(Manga manga) {
        this.mangaName = manga.getMangaName();
        this.mangaDescription = manga.getMangaDescription();
        this.mangaPosterImg = manga.getMangaPosterImg();
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

    public String getMangaPosterImg() {
        return mangaPosterImg;
    }

    public void setMangaPosterImg(String mangaPosterImg) {
        this.mangaPosterImg = mangaPosterImg;
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

    public Boolean getAdultContent() {
        return adultContent;
    }

    public void setAdultContent(Boolean adultContent) {
        this.adultContent = adultContent;
    }

    public String getMangaStatus() {
        return mangaStatus;
    }

    public void setMangaStatus(String mangaStatus) {
        this.mangaStatus = mangaStatus;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<Chapter> getChapter() {
        return chapter;
    }

    public void setChapter(List<Chapter> chapter) {
        this.chapter = chapter;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
}
