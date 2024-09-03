package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserReport;
import com.example.MangaLibrary.repo.UserReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserReportService {

    @Autowired
    UserReportRepo userReportRepo;

    public void reportUser(User reportedUser, User reporterUser, String reason) {
        if (!userReportRepo.existsByReportedUserAndReporterUser(reportedUser, reporterUser)) {
            UserReport userReport = new UserReport();
            userReport.setReportedUser(reportedUser);
            userReport.setReporterUser(reporterUser);
            userReport.setReason(reason);
            userReport.setReportedAt(LocalDateTime.now());
            userReportRepo.save(userReport);
        } else {
            throw new IllegalArgumentException("Ви вже подали скаргу на цього користувача.");
        }
    }
}