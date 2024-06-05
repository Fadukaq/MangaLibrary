package com.example.MangaLibrary.helper;

import com.example.MangaLibrary.models.User;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public class ProfilePicture {

    private MultipartFile profileImage;
    public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }
}
