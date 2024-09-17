package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.CommentReport;
import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepo extends JpaRepository<CommentReport, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);

    List<CommentReport> findByStatus(String status);
}