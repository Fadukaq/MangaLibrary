package com.example.MangaLibrary.controllers;

import ch.qos.logback.core.model.Model;
import com.example.MangaLibrary.models.*;
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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
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
    MangaRepo mangaRepo;
    @Autowired
    CommentRatingRepo commentRatingRepo;
    @Autowired
    RepliesRepo repliesRepo;
    @PostMapping("/comment/{mangaId}/add-comment")
    @ResponseBody
    public ResponseEntity<Comment> addComment(@PathVariable Long mangaId, @RequestParam String text, Principal principal) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        if (!mangaOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Manga manga = mangaOptional.get();

        User user = userRepo.findByUserName(principal.getName());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (text == null || text.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Comment comment = new Comment(text, manga, user, LocalDateTime.now());
        commentRepo.save(comment);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }


    @GetMapping("/comment/{commentId}/rate") /////////////////////////////////////////////////////////////////////
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
