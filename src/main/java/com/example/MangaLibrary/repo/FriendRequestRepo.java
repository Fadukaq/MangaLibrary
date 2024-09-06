package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.helper.user.RequestStatus;
import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.CommentReport;
import com.example.MangaLibrary.models.FriendRequest;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepo extends JpaRepository<FriendRequest, Long> {

    boolean existsBySenderAndReceiverAndStatus(User currentUser, User targetUser, RequestStatus requestStatus);

    List<FriendRequest> findByReceiverAndStatus(User currentUser, RequestStatus requestStatus);

    List<FriendRequest> findBySender(User currentUser);

    List<FriendRequest> findBySenderAndStatus(User currentUser, RequestStatus requestStatus);
}
