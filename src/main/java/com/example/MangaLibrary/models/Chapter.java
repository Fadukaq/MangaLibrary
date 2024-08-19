package com.example.MangaLibrary.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotEmpty(message = "Назва глави не повинна бути порожньою")
    @Size(min=2, max=255, message = "Назва глави має бути від 2 до 255 символів")
    private String title;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manga_id", nullable = false)
    @JsonManagedReference
    private Manga manga;
    private LocalDateTime creationTime;
    String chapterPages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public String getChapterPages() {
        return chapterPages;
    }

    public void setChapterPages(String chapterPages) {
        this.chapterPages = chapterPages;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
