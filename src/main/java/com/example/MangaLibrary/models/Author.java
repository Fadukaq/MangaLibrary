package com.example.MangaLibrary.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Ім'я автора не повинно бути порожнім")
    @Size(min=5,max=512, message = "Ім'я автора має складатися від 10 до 512 символів")
    private String name;

    @NotEmpty(message = "Посилання на картинку не повинно бути порожнім")
    @Size(min=10,max=2048, message = "Посилання на картинку має складатися від 10 до 2048 символів")
    private String urlPicture;

    @NotEmpty(message = "Біографія автора не повинно бути порожнім")
    @Size(min=10,max=2048, message = "Біографія автора має складатися від 10 до 2048 символів")
    private String biography;

    public Author() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlPicture() {
        return urlPicture;
    }

    public void setUrlPicture(String urlPicture) {
        this.urlPicture = urlPicture;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}
