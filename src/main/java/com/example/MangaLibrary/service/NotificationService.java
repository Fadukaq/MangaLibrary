package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.NotificationRepo;
import com.example.MangaLibrary.repo.SubscriptionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @Autowired
    private NotificationRepo notificationRepo;

    public void notifyUsersAboutNewManga(Author author, Manga newManga) {
        List<Subscription> subscriptions = subscriptionRepo.findByAuthor(author);
        for (Subscription subscription : subscriptions) {
            Notification notification = new Notification();
            notification.setUser(subscription.getUser());
            notification.setAuthor(author);
            notification.setManga(newManga);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setNotifyType("AUTHOR");
            notificationRepo.save(notification);
        }
    }

    public void notifyUsersAboutNewChapter(Manga manga, Chapter newChapter) {
        List<Subscription> subscriptions = subscriptionRepo.findByManga(manga);
        for (Subscription subscription : subscriptions) {
            Notification notification = new Notification();
            notification.setUser(subscription.getUser());
            notification.setManga(manga);
            notification.setChapter(newChapter);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setNotifyType("MANGA");
            notificationRepo.save(notification);
        }
    }
}
