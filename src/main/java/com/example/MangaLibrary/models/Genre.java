package com.example.MangaLibrary.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Поле жанр не повинно бути порожнім")
    @Size(min=3,max=255, message = "Жанр має складатися від 3 до 255")
    @Column(nullable = false, unique = true)
    private String genreName;

    public Genre() {}

    public Genre(Genre genre) {
        this.id = genre.getId();
        this.genreName = genre.getGenreName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }
}