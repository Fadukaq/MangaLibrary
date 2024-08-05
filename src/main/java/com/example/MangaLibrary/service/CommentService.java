package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private CommentRatingRepo commentRatingRepo;
    @Autowired
    private CommentReportRepo commentReportRepo;
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

        commentRepo.save(comment);
    }
    public void updateRating(Long commentId, Long userId, int delta) {
        CommentRating existingRating = commentRatingRepo.findByCommentIdAndUserId(commentId, userId);

        if (existingRating != null) {
            if (existingRating.getDelta() == delta) {
                commentRatingRepo.delete(existingRating);
            } else {
                existingRating.setDelta(delta);
                commentRatingRepo.save(existingRating);
            }
        } else {
            Comment comment = commentRepo.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CommentRating newRating = new CommentRating();
            newRating.setComment(comment);
            newRating.setUser(user);
            newRating.setDelta(delta);
            commentRatingRepo.save(newRating);
        }

        updateCommentRating(commentId);
    }
    private void updateCommentRating(Long commentId) {
        int newRatingScore = commentRatingRepo.getSumRatingByCommentId(commentId);

        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setRating(newRatingScore);
        commentRepo.save(comment);
    }
    public boolean reportComment(Long commentId, Long userId, String reason) {
        Comment comment = commentRepo.findById(commentId).orElse(null);
        User user = userRepo.findById(userId).orElse(null);

        if (comment == null || user == null) {
            return false;
        }

        if (commentReportRepo.existsByCommentAndUser(comment, user)) {
            return false;
        }

        CommentReport report = new CommentReport();
        report.setComment(comment);
        report.setUser(user);
        report.setReportedAt(LocalDateTime.now());
        report.setReason(reason);

        commentReportRepo.save(report);
        return true;
    }
    public Map<Long, Integer> getUserRatingsForComments(Long userId, List<Long> commentIds) {
        List<CommentRating> ratings = commentRatingRepo.findByUserIdAndCommentIdIn(userId, commentIds);
        return ratings.stream()
                .collect(Collectors.toMap(
                        rating -> rating.getComment().getId(),
                        CommentRating::getDelta
                ));
    }
    public List<Comment> getCommentsByMangaId(Long mangaId) {
        return commentRepo.findByMangaId(mangaId);
    }
}