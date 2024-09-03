package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReportRepo  extends JpaRepository<UserReport, Long> {
    boolean existsByReportedUserAndReporterUser(User reportedUser, User reporterUser);
}