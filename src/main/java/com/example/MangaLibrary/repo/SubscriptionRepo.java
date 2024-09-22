package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {

    List<Subscription> findByManga(Manga manga);

    List<Subscription> findByAuthor(Author author);

    Subscription findByUserAndManga(User user, Manga manga);

    Subscription findByUserAndAuthor(User user, Author author);

    boolean existsByUserAndAuthor(User user, Author author);
}
