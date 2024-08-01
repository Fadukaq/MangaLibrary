package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.Rating;
import com.example.MangaLibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepo extends JpaRepository<Rating, Long> {
    Optional<Rating> findByMangaAndUser(Manga manga, User user);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.manga = :manga")
    double findAverageRatingByManga(@Param("manga") Manga manga);

    int countByManga(Manga manga);
    void deleteByMangaIdAndUserId(Long mangaId, Long userId);

    int countByMangaId(Long mangaId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.manga.id = :mangaId")
    double findAverageByMangaId(@Param("mangaId") Long mangaId);
}
