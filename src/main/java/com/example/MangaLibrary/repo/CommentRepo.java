package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    List<Comment> findByMangaId(Long mangaId);
    Optional<Comment> findById(Long id);
    Optional<Comment> findByIdAndUser(Long id, User user);
}