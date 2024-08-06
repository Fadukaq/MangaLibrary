package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Comment;
import com.example.MangaLibrary.models.Replies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepliesRepo extends JpaRepository<Replies, Long> {
    List<Replies> findByParentComment(Comment parentComment);

    List<Replies> findByParentCommentId(long id);
}
