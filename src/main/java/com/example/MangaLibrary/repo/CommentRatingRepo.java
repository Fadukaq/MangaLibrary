package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.CommentRating;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRatingRepo extends JpaRepository<CommentRating, Long> {

    boolean existsByCommentAndUser(Comment comment, User user);

    Optional<CommentRating> findByCommentAndUser(Comment comment, User user);

    CommentRating findByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT COALESCE(SUM(cr.delta), 0) FROM CommentRating cr WHERE cr.comment.id = :commentId")
    int getSumRatingByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT COUNT(cr) FROM CommentRating cr WHERE cr.comment.id = :commentId AND cr.delta = :delta")
    int getCountByCommentIdAndDelta(@Param("commentId") Long commentId, @Param("delta") int delta);
    long countByCommentIdAndDelta(Long commentId, int delta);
    List<CommentRating> findByUserIdAndCommentIdIn(Long userId, List<Long> commentIds);
}
