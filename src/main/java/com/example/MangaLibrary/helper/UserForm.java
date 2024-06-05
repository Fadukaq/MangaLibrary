package com.example.MangaLibrary.helper;

import com.example.MangaLibrary.models.User;
import jakarta.validation.Valid;

public class UserForm {
    @Valid
    User user;
    @Valid
    private ProfilePicture profilePicture;
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProfilePicture getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(ProfilePicture profilePicture) {
        this.profilePicture = profilePicture;
    }
}
