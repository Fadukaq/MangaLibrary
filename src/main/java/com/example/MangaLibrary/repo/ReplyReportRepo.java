package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Replies;
import com.example.MangaLibrary.models.ReplyReport;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyReportRepo extends JpaRepository<ReplyReport, Long> {
    boolean existsByReplyAndUser(Replies reply, User user);

    List<ReplyReport> findByStatus(String status);
}