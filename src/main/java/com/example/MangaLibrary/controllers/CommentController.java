package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.CommentRating;
import com.example.MangaLibrary.models.Replies;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.*;
import com.example.MangaLibrary.service.CommentService;
import com.example.MangaLibrary.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class CommentController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    CommentService commentService;
    @Autowired
    CommentRepo commentRepo;
    @Autowired
    CommentRatingRepo commentRatingRepo;
    @Autowired
    RepliesRepo repliesRepo;
    @GetMapping("/manga/{mangaId}/comments")
    public @ResponseBody Map<String, Object> getComments(
            @PathVariable Long mangaId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "4") int size,
            @RequestParam(required = false, defaultValue = "byNew") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if ("byRating".equals(sortBy)) {
            pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "rating"));
        }
        Page<Comment> commentPage = commentRepo.findByMangaId(mangaId, pageable);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        Map<Long, Integer> userRatings = new HashMap<>();
        Map<Long, Map<String, Integer>> commentRatings = new HashMap<>();
        Map<Long, List<Replies>> commentReplies = new HashMap<>();

        for (Comment comment : commentPage.getContent()) {
            CommentRating userRating = commentRatingRepo.findByCommentIdAndUserId(comment.getId(), user.getId());
            userRatings.put(comment.getId(), userRating != null ? userRating.getDelta() : 0);

            long upvotes = commentRatingRepo.countByCommentIdAndDelta(comment.getId(), 1);
            long downvotes = commentRatingRepo.countByCommentIdAndDelta(comment.getId(), -1);

            Map<String, Integer> ratingInfo = new HashMap<>();
            ratingInfo.put("upvotes", (int) upvotes);
            ratingInfo.put("downvotes", (int) downvotes);
            commentRatings.put(comment.getId(), ratingInfo);

            List<Replies> replies = repliesRepo.findByParentCommentId(comment.getId());
            commentReplies.put(comment.getId(), replies);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("comments", commentPage.getContent());
        response.put("userRatings", userRatings);
        response.put("commentRatings", commentRatings);
        response.put("commentReplies", commentReplies);
        response.put("hasMore", commentPage.hasNext());

        return response;
    }

    @GetMapping("/manga/comment/{commentId}/rate") /////////////////////////////////////////////////////////////////////
    public ResponseEntity<Map<String, Object>> updateRatingGET(@PathVariable Long commentId,
                                                               @RequestParam Long userId,
                                                               @RequestParam int delta) {
        commentService.updateRating(commentId, userId, delta);

        Map<String, Object> ratingInfo = new HashMap<>();
        int upvotes = commentRatingRepo.getCountByCommentIdAndDelta(commentId, 1);
        int downvotes = commentRatingRepo.getCountByCommentIdAndDelta(commentId, -1);
        int newRatingScore = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"))
                .getRating();

        String ratingTitle = "Плюсів: " + upvotes + " | Мінусів: " + downvotes;

        ratingInfo.put("newRatingScore", newRatingScore);
        ratingInfo.put("ratingTitle", ratingTitle);
        return ResponseEntity.ok(ratingInfo);
    }

    @PostMapping("/comment/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        Optional<Comment> comment =  commentRepo.findById(id);
        if(comment.isPresent())
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);
            Comment currentComment = comment.get();
            if (!currentComment.getUser().equals(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви не можете видалити цей коментар");
            }else {
                commentRepo.deleteById(id);
                return ResponseEntity.ok().body("Коментар видалено.");
            }
        }
        return ResponseEntity.ok().body("Коментар не знайдено.");
    }

    @PostMapping("/comment/report")
    public ResponseEntity<String> reportComment(
            @RequestParam Long commentId,
            @RequestParam Long userId,
            @RequestParam String reason) {
        boolean success = commentService.reportComment(commentId, userId, reason);
        if (success) {
            return ResponseEntity.ok("Коментар успішно надіслано.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви вже повідомляли про цей коментар.");
        }
    }

    @PostMapping("/comment/edit")
    @ResponseBody
    public ResponseEntity<String> editComment(
            @RequestParam Long commentIdEdit,
            @RequestParam String text) {
        boolean success = commentService.updateComment(commentIdEdit, text);
        if (success) {
            return ResponseEntity.ok("Коментар успішно відредаговано.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви не можете редагувати цей коментар.");
        }
    }
}
