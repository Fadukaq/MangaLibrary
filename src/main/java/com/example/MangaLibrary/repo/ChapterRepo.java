package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Manga;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChapterRepo extends JpaRepository<Chapter, Long> {
    List<Chapter> findByMangaId(Long mangaId);
    boolean existsByMangaIdAndTitle(Long mangaId, String title);
    boolean existsByMangaIdAndTitleAndIdNot(Long mangaId, String title, Long id);

    @Query("SELECT c FROM Chapter c ORDER BY c.creationTime DESC")
    List<Chapter> findLatestChapters(Pageable pageable);

    Optional<Chapter> findTopByMangaIdAndIdLessThanOrderByIdDesc(Long mangaId, Long currentChapterId);

    Optional<Chapter> findTopByMangaIdAndIdGreaterThanOrderByIdAsc(Long mangaId, Long currentChapterId);
}