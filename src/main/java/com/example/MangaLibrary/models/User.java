package com.example.MangaLibrary.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotEmpty(message = "Поле ім'я юзера не повинно бути порожнім")
    @Size(min=2,max=120, message = "Ім'я юзера має складатися від 2 до 120 символів")
    private String userName;

    @NotEmpty(message = "Поле пароль не повинно бути порожнім")
    @Size(min=2,max=255, message = "Поле пароль має складатися від 2 до 255 символів")
    private String userPassword;
    private String userRole;

    @ElementCollection
    private List<String> mangaReading = new ArrayList<>();

    @ElementCollection
    private List<String> mangaWantToRead = new ArrayList<>();

    @ElementCollection
    private List<String> mangaStoppedReading = new ArrayList<>();

    @ElementCollection
    private List<String> mangaRecited = new ArrayList<>();

    public User() {}
    public User(User user) {
        this.userName = user.getUserName();
        this.userPassword = user.getUserPassword();
        this.userRole= user.getUserRole();
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public List<String> getMangaReading() {
        return mangaReading;
    }

    public void setMangaReading(List<String> mangaReading) {
        this.mangaReading = mangaReading;
    }

    public List<String> getMangaWantToRead() {
        return mangaWantToRead;
    }

    public void setMangaWantToRead(List<String> mangaWantToRead) {
        this.mangaWantToRead = mangaWantToRead;
    }

    public List<String> getMangaStoppedReading() {
        return mangaStoppedReading;
    }

    public void setMangaStoppedReading(List<String> mangaStoppedReading) {
        this.mangaStoppedReading = mangaStoppedReading;
    }

    public List<String> getMangaRecited() {
        return mangaRecited;
    }

    public void setMangaRecited(List<String> mangaRecited) {
        this.mangaRecited = mangaRecited;
    }
}
