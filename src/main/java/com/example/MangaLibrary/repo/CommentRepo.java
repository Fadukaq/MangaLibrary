package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {
    List<Comment> findByMangaId(Long mangaId);
}
