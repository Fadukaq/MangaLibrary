package com.example.MangaLibrary.models;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotEmpty(message = "Текст коментаря не повинен бути порожнім")
    @Size(min = 1, max = 1000, message = "Коментар повинен містити від 1 до 1000 символів")
    private String text;

    @ManyToOne
    @JoinColumn(name = "manga_id")
    @JsonIgnore
    private Manga manga;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private User user;
    private LocalDateTime createdAt;
    @JsonProperty("userName")
    public String getUserName() {
        return user != null ? user.getUserName() : "Unknown User";
    }
    @JsonProperty("ProfilePicture")
    public String getUserProfilePicture() {
        return user != null ? user.getProfilePicture() : "<i class='fa-solid fa-user-circle'></i>";
    }
    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Comment() {}

    public Comment(String text, Manga manga, User user, LocalDateTime createdAt) {
        this.text = text;
        this.manga = manga;
        this.user = user;
        this.createdAt = createdAt;
    }
}
