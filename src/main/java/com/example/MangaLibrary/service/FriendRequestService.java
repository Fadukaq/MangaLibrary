package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.CommentReport;
import com.example.MangaLibrary.models.FriendRequest;
import com.example.MangaLibrary.repo.FriendRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FriendRequestService {

    @Autowired
    FriendRequestRepo friendRequestRepo;

}
