package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.Replies;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.CommentRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.RepliesRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.CommentService;
import com.example.MangaLibrary.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    MangaRepo mangaRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;
    @Autowired
    CommentRepo commentRepo;
    @Autowired
    RepliesRepo repliesRepo;
    @PostMapping("/manga/comment/{id}/delete")
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

    @GetMapping("/comment/{commentId}/report") /////////////////////////////////////
    public ResponseEntity<String> reportComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestParam String reason) {

        boolean success = commentService.reportComment(commentId, userId, reason);
        if (success) {
            return ResponseEntity.ok("Коментар успішно надіслано.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви вже повідомляли про цей коментар.");
        }
    }

    @GetMapping("/manga/comments/{id}/edit") //////////////////////////////
    @ResponseBody
    public ResponseEntity<String> editComment(@PathVariable Long id, @RequestParam String content) {
        boolean success = commentService.updateComment(id, content);
        if (success) {
            return ResponseEntity.ok("Коментар успішно відредаговано.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви не можете редагувати цей коментар.");
        }
    }

    @PostMapping("/manga/reply/{id}/delete")
    public @ResponseBody Map<String, Boolean> deleteReply(@PathVariable Long id, Principal principal) {
        Optional<Replies> replyOptional = repliesRepo.findById(id);
        if (replyOptional.isPresent()) {
            Replies reply = replyOptional.get();
            String currentUserName = principal.getName();
            if (reply.getUser().getUserName().equals(currentUserName)) {
                repliesRepo.delete(reply);
                return Collections.singletonMap("success", true);
            }
        }
        return Collections.singletonMap("success", false);
    }
    @GetMapping("/reply/{id}/edit") /////////////////////////////////////////////////////////////////////
    @ResponseBody
    public ResponseEntity<String> editReply(@PathVariable Long id, @RequestParam String content, Principal principal) {
        String currentUserName = principal.getName();
        Replies reply = repliesRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

        User replyAuthor = userRepo.findById(reply.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUserName.equals(replyAuthor.getUserName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to edit this reply.");
        }
        User mentionedUser = userService.extractUserFromComment(content);
        if (mentionedUser != null) {
            content = content.replace("@" + mentionedUser.getUserName(), "@" + mentionedUser.getId());
        }

        reply.setText(content);
        repliesRepo.save(reply);
        return ResponseEntity.ok("Reply edited successfully.");
    }
    @GetMapping("/reply/{replyId}/report") //////////////////////////////////////////////////////////////////////////////
    public ResponseEntity<String> reportReply(
            @PathVariable Long replyId,
            @RequestParam Long userId,
            @RequestParam String reason) {
        boolean success = commentService.reportReply(replyId, userId, reason);
        if (success) {
            return ResponseEntity.ok("Reply reported successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have already reported this reply");
        }
    }
}
