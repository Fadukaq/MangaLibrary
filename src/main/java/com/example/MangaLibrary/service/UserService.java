package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired UserRepo userRepo;

    public boolean activateUser(String code) {
        User user = userRepo.findByVerificationToken(code);
        if(user == null){
            return false;
        }
        user.setVerificationToken(null);
        user.setEnabled(true);
        userRepo.save(user);
        return true;
    }
}
