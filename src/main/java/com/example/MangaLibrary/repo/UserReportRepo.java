package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReportRepo  extends JpaRepository<UserReport, Long> {
    boolean existsByReportedUserAndReporterUser(User reportedUser, User reporterUser);
    List<UserReport> findByStatus(String status);

    List<UserReport> findByReportedUserId(Long userId);
}