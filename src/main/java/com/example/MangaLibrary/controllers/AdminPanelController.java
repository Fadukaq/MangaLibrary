package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.CommentReport;
import com.example.MangaLibrary.models.ReplyReport;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.models.UserReport;
import com.example.MangaLibrary.repo.*;
import com.example.MangaLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminPanelController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    UserReportRepo userReportRepo;
    @Autowired
    CommentReportRepo commentReportRepo;
    @Autowired
    ReplyReportRepo replyReportRepo;
    @Autowired
    UserService userService;
    @GetMapping("/admin-panel")
    public String adminPanelGet(@RequestParam(name = "username", required = false) String username, Model model) {
        List<User> users;
        if (username != null && !username.isEmpty()) {
            users = userRepo.findByUserNameContaining(username);
        } else {
            users = userRepo.findAll();
        }
        List<UserReport> usersResolved = userReportRepo.findByStatus("RESOLVED");
        List<CommentReport> commentsResolved = commentReportRepo.findByStatus("RESOLVED");
        List<ReplyReport> repliesResolved = replyReportRepo.findByStatus("RESOLVED");


        List<UserReport> usersReported = userReportRepo.findByStatus("PENDING");
        List<CommentReport> commentsReported = commentReportRepo.findByStatus("PENDING");
        List<ReplyReport> repliesReported = replyReportRepo.findByStatus("PENDING");
        model.addAttribute("users", users);
        model.addAttribute("usersReported", usersReported);
        model.addAttribute("commentsReport", commentsReported);
        model.addAttribute("repliesReport", repliesReported);

        model.addAttribute("usersResolved", usersResolved);
        model.addAttribute("commentsResolved", commentsResolved);
        model.addAttribute("repliesResolved", repliesResolved);
        model.addAttribute("username", username);
        return "user/admin-panel";
    }

    @PostMapping("/admin-panel")
    public String adminPanelPost(Model model) {
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        return "user/admin-panel";
    }
    @PostMapping("/admin-panel/update-user")
    public String updateUser(Long userId, String role, String enabled, String mainPanel , String secondPanel, Model model) {
        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isPresent()) {
            User userToUpdate = optionalUser.get();
            String currentUserName = userService.getCurrentUserName();

            if (userToUpdate.getUserName().equals(currentUserName) && userToUpdate.getUserRole().equals("ADMIN")) {
                model.addAttribute("users", userRepo.findAll());
                return "redirect:/admin-panel" + mainPanel +secondPanel;
            }

            Boolean enabledValue = Boolean.valueOf(enabled);

            if (!userService.isValidateRoleAndEnabled(userToUpdate, role, enabled)) {
                model.addAttribute("users", userRepo.findAll());
                return "redirect:/admin-panel" + mainPanel +secondPanel;
            }

            if (role != null && userToUpdate.isEnabled()) {
                userService.updateUserRole(userId, role);
            }
            if (enabled != null) {
                userService.updateUserEnabledStatus(userId, enabledValue);
                //if(enabled.equals("false")) {
                //userService.deleteAllCommentsByUserId(userId);
                //}
            }
        }
        model.addAttribute("users", userRepo.findAll());
        if(secondPanel!=null){
            return "redirect:/admin-panel" + mainPanel +secondPanel;
        } else{
            return "redirect:/admin-panel" + mainPanel;
        }
    }
    @PostMapping("/admin-panel/update-status-user")
    public String updateReportStatus(@RequestParam("reportId") Long reportId, @RequestParam("status") String status, String mainPanel , String secondPanel , RedirectAttributes redirectAttributes) {
        Optional<UserReport> reportOpt = userReportRepo.findById(reportId);
        if (reportOpt.isPresent()) {
            UserReport report = reportOpt.get();
            report.setStatus(status);
            userReportRepo.save(report);
            redirectAttributes.addFlashAttribute("message", "Статус звіту успішно оновлено.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Звіт не знайдено.");
        }
        return "redirect:/admin-panel" + mainPanel +secondPanel;
    }
    @PostMapping("/admin-panel/update-status-comment")
    public String updateReportStatusComment(
            @RequestParam("reportId") Long reportId,
            @RequestParam("status") String status,
            @RequestParam("mainPanel") String mainPanel,
            @RequestParam("secondPanel") String secondPanel,
            RedirectAttributes redirectAttributes) {

        Optional<CommentReport> reportOpt = commentReportRepo.findById(reportId);
        if (reportOpt.isPresent()) {
            CommentReport report = reportOpt.get();
            report.setStatus(status);
            commentReportRepo.save(report);

            redirectAttributes.addFlashAttribute("message", "Статус коментаря успішно оновлено.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Коментар не знайдено.");
        }
        String redirectUrl = String.format("/admin-panel%s%s", mainPanel, secondPanel);
        return "redirect:" + redirectUrl;
    }
    @PostMapping("/admin-panel/update-status-reply")
    public String updateReportStatusReply(
            @RequestParam("reportId") Long reportId,
            @RequestParam("status") String status,
            @RequestParam("mainPanel") String mainPanel,
            @RequestParam("secondPanel") String secondPanel,
            RedirectAttributes redirectAttributes) {

        Optional<ReplyReport> reportOpt = replyReportRepo.findById(reportId);
        if (reportOpt.isPresent()) {
            ReplyReport report = reportOpt.get();
            report.setStatus(status);
            replyReportRepo.save(report);

            redirectAttributes.addFlashAttribute("message", "Статус коментаря успішно оновлено.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Коментар не знайдено.");
        }
        String redirectUrl = String.format("/admin-panel%s%s", mainPanel, secondPanel);
        return "redirect:" + redirectUrl;
    }
}