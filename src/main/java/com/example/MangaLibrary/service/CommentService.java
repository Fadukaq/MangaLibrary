package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.CommentRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepo commentRepository;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MangaRepo mangaRepo;
    public void addComment(Long mangaId, String text, Long userId) {
        Manga manga = mangaRepo.findById(mangaId)
                .orElseThrow(() -> new EntityNotFoundException("Manga not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setText(text);
        comment.setManga(manga);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    public List<Comment> getCommentsByMangaId(Long mangaId) {
        return commentRepository.findByMangaId(mangaId);
    }
}