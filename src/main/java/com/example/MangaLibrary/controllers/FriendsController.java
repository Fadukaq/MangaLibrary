package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.helper.user.RequestStatus;
import com.example.MangaLibrary.models.FriendRequest;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.FriendRequestRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.FriendRequestService;
import com.example.MangaLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class FriendsController {
    @Autowired
    FriendRequestRepo friendRequestRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    private FriendRequestService friendRequestService;
    @Autowired
    private UserService userService;
    @PostMapping("/friends/sendFriendRequest")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long currentUserId, @RequestParam Long userId, Principal principal) {
        User currentUser = userRepo.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Поточний користувач не знайдений"));

        if (!currentUser.getUserName().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"success\": false, \"message\": \"Немає прав для виконання цієї дії\"}");
        }
        User targetUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Цільовий користувач не знайдений"));

        if (friendRequestRepo.existsBySenderAndReceiverAndStatus(currentUser, targetUser, RequestStatus.PENDING)) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"Запит на дружбу вже надіслано\"}");
        }
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(currentUser);
        friendRequest.setReceiver(targetUser);
        friendRequest.setRequestDate(LocalDateTime.now());
        friendRequest.setStatus(RequestStatus.PENDING);
        friendRequestRepo.save(friendRequest);
        return ResponseEntity.ok("{\"success\": true, \"message\": \"Запит на дружбу успішно надіслано\"}");
    }

    @PostMapping("/friends/respondRequest")
    public ResponseEntity<Map<String, Object>> respondToRequest(@RequestParam Long requestId, @RequestParam String response, Principal principal) {
        Map<String, Object> responseMap = new HashMap<>();
        Optional<FriendRequest> requestOptional = friendRequestRepo.findById(requestId);
        User currentUser = userRepo.findByUserName(principal.getName());

        if (requestOptional.isPresent()) {
            FriendRequest request = requestOptional.get();
            if (request.getReceiver().equals(currentUser)) {
                if (response.equals("accept")) {
                    request.setStatus(RequestStatus.ACCEPTED);
                    currentUser.getFriends().add(request.getSender());
                    request.getSender().getFriends().add(currentUser);

                    userRepo.save(currentUser);
                    userRepo.save(request.getSender());
                    friendRequestRepo.save(request);

                    responseMap.put("success", true);
                    responseMap.put("message", "Запит на дружбу прийнятий.");
                    return ResponseEntity.ok(responseMap);
                } else if (response.equals("decline")) {
                    request.setStatus(RequestStatus.DECLINED);
                    friendRequestRepo.save(request);

                    responseMap.put("success", true);
                    responseMap.put("message", "Запит на дружбу відхилений.");
                    return ResponseEntity.ok(responseMap);
                }
            }
        }
        responseMap.put("success", false);
        responseMap.put("message", "Неможливо відповісти на запит.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMap);
    }

    @PostMapping("/friends/remove")
    @ResponseBody
    public Map<String, Object> removeFriend(@RequestParam Long friendId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = userRepo.findByUserName(principal.getName());

            User friendToRemove = userRepo.findById(friendId)
                    .orElseThrow(() -> new RuntimeException("Друг не знайдений"));

            currentUser.getFriends().remove(friendToRemove);

            friendToRemove.getFriends().remove(currentUser);

            userRepo.save(currentUser);
            userRepo.save(friendToRemove);

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/friends/cancelRequest")
    @ResponseBody
    public Map<String, Object> cancelRequest(@RequestParam Long requestId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userRepo.findByUserName(principal.getName());
        Optional<FriendRequest> requestOptional = friendRequestRepo.findById(requestId);

        if (requestOptional.isPresent()) {
            FriendRequest request = requestOptional.get();
            if (request.getSender().equals(currentUser)) {
                friendRequestRepo.delete(request);
                response.put("success", true);
                response.put("message", "Запит успішно скасовано.");
            } else {
                response.put("success", false);
                response.put("message", "Неможливо скасувати запит.");
            }
        } else {
            response.put("success", false);
            response.put("message", "Запит не знайдено.");
        }

        return response;
    }
}