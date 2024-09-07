package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.Replies;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.CommentRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.RepliesRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.ReplyService;
import com.example.MangaLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class ReplyController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    RepliesRepo repliesRepo;
    @Autowired
    MangaRepo mangaRepo;
    @Autowired
    CommentRepo commentRepo;
    @Autowired
    UserService userService;
    @Autowired
    ReplyService replyService;

    @GetMapping("/manga/comment/reply") ///////////////////////////////////////////////////////////////////////////////
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReply(
            @RequestParam("text") String text,
            @RequestParam("parentCommentId") Long parentCommentId,
            @RequestParam("mangaId") Long mangaId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Comment parentComment = commentRepo.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = userRepo.findByUserName(userDetails.getUsername());

            String mentionedUsername = null;
            Pattern pattern = Pattern.compile("@(\\w+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                mentionedUsername = matcher.group(1);
            }
            User userReplied = null;
            if (mentionedUsername != null) {
                userReplied = userRepo.findByUserName(mentionedUsername);
                if (userReplied != null) {
                    text = text.replace("@" + mentionedUsername, "@" + userReplied.getId());
                }
            }

            Replies reply = new Replies();
            reply.setText(text);
            reply.setParentComment(parentComment);
            reply.setUser(userRepo.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
            reply.setCreatedAt(LocalDateTime.now());
            reply.setManga(mangaRepo.findById(mangaId)
                    .orElseThrow(() -> new RuntimeException("Manga not found")));
            repliesRepo.save(reply);

            response.put("success", true);
            response.put("reply", reply);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при добавлении ответа.");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reply/{id}/delete")
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
    @PostMapping("/reply/edit")
    @ResponseBody
    public ResponseEntity<String> editReply( @RequestParam Long replyIdEdit,  @RequestParam("text") String content, Principal principal) {
        String currentUserName = principal.getName();
        Optional<Replies> replyOptional = repliesRepo.findById(replyIdEdit);
        if(replyOptional.isPresent()) {
            Replies reply = replyOptional.get();
            Optional<User> replyAuthorOptional = userRepo.findById(reply.getUserId());
            if(replyAuthorOptional.isPresent()){
                User replyAuthor = replyAuthorOptional.get();
                if (!currentUserName.equals(replyAuthor.getUserName())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви не маєте права редагувати цю відповідь.");
                }
                User mentionedUser = userService.extractUserFromComment(content);
                if (mentionedUser != null) {
                    content = content.replace("@" + mentionedUser.getUserName(), "@" + mentionedUser.getId());
                }

                reply.setText(content);
                repliesRepo.save(reply);
                return ResponseEntity.ok("Відповідь успішно відредаговано.");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Автора відповіді або відповідь не знайдено.");
    }
    @PostMapping("/reply/report")
    public ResponseEntity<String> reportReply(
            @RequestParam Long replyId,
            @RequestParam Long userId,
            @RequestParam String reason) {
        boolean success = replyService.reportReply(replyId, userId, reason);
        if (success) {
            return ResponseEntity.ok("Скарга на відповідь надіслано успішно");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ви вже надіслали скаргу на цю відповідь.");
        }
    }

}
