package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Manga;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MangaRepo extends CrudRepository<Manga, Long> {
    @Query("SELECT m FROM Manga m JOIN m.genres g WHERE g.genreName IN :genreNames GROUP BY m HAVING COUNT(DISTINCT g.genreName) = :genreCount")
    Page<Manga> findAllByGenreNames(@Param("genreNames") String[] genreNames, @Param("genreCount") long genreCount, Pageable pageable);

    @Query("SELECT m FROM Manga m JOIN m.genres g WHERE g.genreName = :genreName")
    Page<Manga> findAllByGenreNamePagination(@Param("genreName") String genreName, Pageable pageable);

    Page<Manga> findByMangaNameContainingIgnoreCase(String mangaName, Pageable pageable);
    Manga findByMangaName(String mangaName);
    Page<Manga> findAll(Pageable pageable);
}