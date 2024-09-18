package com.example.MangaLibrary.models;

import jakarta.persistence.*;

@Entity
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String backgroundImage;
    private String profilePrivacy;
    private String readStyle;
    private String pageStyle;
    private Boolean adultContentAgreement;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getProfilePrivacy() {
        return profilePrivacy;
    }

    public void setProfilePrivacy(String profilePrivacy) {
        this.profilePrivacy = profilePrivacy;
    }

    public String getReadStyle() {
        return readStyle;
    }

    public void setReadStyle(String readStyle) {
        this.readStyle = readStyle;
    }

    public Boolean getAdultContentAgreement() {
        return adultContentAgreement;
    }

    public void setAdultContentAgreement(Boolean adultContentAgreement) {
        this.adultContentAgreement = adultContentAgreement;
    }

    public String getPageStyle() {
        return pageStyle;
    }

    public void setPageStyle(String pageStyle) {
        this.pageStyle = pageStyle;
    }
}
