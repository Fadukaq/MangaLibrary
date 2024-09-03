package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.Replies;
import com.example.MangaLibrary.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepliesRepo extends JpaRepository<Replies, Long> {
    List<Replies> findByParentComment(Comment parentComment);

    List<Replies> findByParentCommentId(long id);

    Page<Replies> findByUser(User user, Pageable pageable);
}
