package com.example.MangaLibrary.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotEmpty(message = "Поле ім'я юзера не повинно бути порожнім")
    @Size(min=4,max=64, message = "Ім'я юзера має складатися від 4 до 64 символів")
    private String userName;
    private String ProfilePicture;
    @NotEmpty(message = "Поле пароль не повинно бути порожнім")
    @Size(min=10,max=255, message = "Поле пароль має складатися від 10 до 255 символів")
    private String userPassword;
    private String userRole;
    @NotEmpty(message = "Поле пошта не повинно бути порожнім")
    @Email(message = "Поле email повинно бути дійсним")
    private String email;
    private boolean enabled;
    private String verificationToken;
    @NotEmpty(message = "Поле про себе не повинно бути порожнім")
    @Size(min=2,max=256, message = "Поле про себе має складатися від 2 до 256 символів")
    private String about ="...";
    @ElementCollection
    private List<String> mangaReading = new ArrayList<>();

    @ElementCollection
    private List<String> mangaWantToRead = new ArrayList<>();

    @ElementCollection
    private List<String> mangaStoppedReading = new ArrayList<>();

    @ElementCollection
    private List<String> mangaRecited = new ArrayList<>();
    @ElementCollection
    private List<String> mangaFavorites = new ArrayList<>();
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSettings userSettings;
    private String resetCode;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @ManyToMany
    @JoinTable(
            name = "friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    @JsonManagedReference
    private List<User> friends = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FriendRequest> sentRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FriendRequest> receivedRequests = new ArrayList<>();

    public User() {}
    public User(User user) {
        this.userName = user.getUserName();
        this.userPassword = user.getUserPassword();
        this.userRole= user.getUserRole();
        this.registrationDate = LocalDateTime.now();
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

    public List<String> getMangaFavorites() {
        return mangaFavorites;
    }

    public void setMangaFavorites(List<String> mangaFavorites) {
        this.mangaFavorites = mangaFavorites;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getProfilePicture() {
        return ProfilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        ProfilePicture = profilePicture;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }



    public void setSentRequests(List<FriendRequest> sentRequests) {
        this.sentRequests = sentRequests;
    }

    public List<FriendRequest> getReceivedRequests() {
        return receivedRequests;
    }

    public void setReceivedRequests(List<FriendRequest> receivedRequests) {
        this.receivedRequests = receivedRequests;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public List<FriendRequest> getSentRequests() {
        return sentRequests;
    }
}
