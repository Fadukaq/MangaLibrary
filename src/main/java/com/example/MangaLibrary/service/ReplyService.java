package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReplyService {
    @Autowired
    private RepliesRepo repliesRepo;
    @Autowired
    private ReplyReportRepo replyReportRepo;
    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private MangaRepo mangaRepo;
    public Replies addReply(Long parentCommentId, String text, Long userId, Long mangaId) {
        Comment parentComment = commentRepo.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Manga manga = mangaRepo.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Manga not found"));

        Replies reply = new Replies();
        reply.setText(text);
        reply.setParentComment(parentComment);
        reply.setUser(user);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setManga(manga);

        return repliesRepo.save(reply);
    }
    public void updateReply(Long replyId, String newText, String currentUsername) {
        Replies reply = repliesRepo.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

        User replyAuthor = userRepo.findById(reply.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!replyAuthor.getUserName().equals(currentUsername)) {
            return;
        }

        reply.setText(newText);
        repliesRepo.save(reply);
    }

    public void deleteReply(Long replyId, String currentUsername) {
        Replies reply = repliesRepo.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

        User replyAuthor = userRepo.findById(reply.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!replyAuthor.getUserName().equals(currentUsername)) {
            return;
        }

        repliesRepo.delete(reply);
    }

    public boolean reportReply(Long replyId, Long userId, String reason) {
        Replies reply = repliesRepo.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean alreadyReported = replyReportRepo.existsByReplyAndUser(reply, user);

        if (alreadyReported) {
            return false;
        }

        ReplyReport replyReport = new ReplyReport();
        replyReport.setReply(reply);
        replyReport.setUser(user);
        replyReport.setReportedAt(LocalDateTime.now());
        replyReport.setReason(reason);

        replyReportRepo.save(replyReport);
        return true;
    }
    public String convertUserIdsToUsernames(String text) {
        final Pattern userIdPattern = Pattern.compile("@(\\d+)");
        final Matcher matcher = userIdPattern.matcher(text);

        StringBuffer result = new StringBuffer();
        int lastMatchEnd = 0;
        boolean hasMatched = false;
        while (matcher.find()) {
            long userId = Long.parseLong(matcher.group(1));
            String username = userService.getUsernameById(userId);
            if (!hasMatched) {
                result.append(text.substring(lastMatchEnd, matcher.start()));
                hasMatched = true;
            } else {
                result.append("<span class='user-text'>")
                        .append(text.substring(lastMatchEnd, matcher.start()))
                        .append("</span>");
            }
            String replacement = String.format(
                    "<a href='/profile/%d' class='user-mention'>@%s</a>",
                    userId,
                    username
            );
            result.append(replacement);
            lastMatchEnd = matcher.end();
        }
        if (lastMatchEnd < text.length()) {
            result.append("<span class='user-text'>")
                    .append(text.substring(lastMatchEnd))
                    .append("</span>");
        }
        return result.toString();
    }
}
