package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepo extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUser(User user);
}
