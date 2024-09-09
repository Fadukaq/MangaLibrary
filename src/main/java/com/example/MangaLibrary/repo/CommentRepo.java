package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    Page<Comment> findByMangaId(Long mangaId, Pageable pageable);
    Optional<Comment> findById(Long id);
    Page<Comment> findByUserId(Long userId, Pageable pageable);

    Page<Comment> findByUser(User user, Pageable pageable);

    List<Comment> findByMangaIdOrderByCreatedAtDesc(Long id);
}