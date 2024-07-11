package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepo extends JpaRepository<Chapter, Long> {
    List<Chapter> findByMangaId(Long mangaId);
    boolean existsByMangaIdAndTitle(Long mangaId, String title);
    boolean existsByMangaIdAndTitleAndIdNot(Long mangaId, String title, Long id);


}