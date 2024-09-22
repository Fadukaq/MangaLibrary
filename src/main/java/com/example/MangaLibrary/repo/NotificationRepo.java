package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Notification;
import com.example.MangaLibrary.models.Subscription;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
}
