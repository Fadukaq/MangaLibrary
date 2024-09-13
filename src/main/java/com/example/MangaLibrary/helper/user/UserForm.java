package com.example.MangaLibrary.helper.user;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserSettings;
import jakarta.validation.Valid;

public class UserForm {
    @Valid
    private User user;
    private UserSettings userSettings;
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

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}
