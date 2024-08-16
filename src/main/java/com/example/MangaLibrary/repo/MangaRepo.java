package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Author;
import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface MangaRepo extends CrudRepository<Manga, Long> {
    @Query("SELECT m FROM Manga m JOIN m.genres g WHERE g.genreName = :genreName")
    Page<Manga> findAllByGenreNamePagination(@Param("genreName") String genreName, Pageable pageable);
    @Query("SELECT m FROM Manga m WHERE m.author.id = :authorId")
    Page<Manga> findAllByAuthorIdPagination(@Param("authorId") Long authorId, Pageable pageable);
    @Query("SELECT m FROM Manga m JOIN m.genres g WHERE g.genreName = :genreName")
    List<Manga> findMangasByGenreName(@Param("genreName") String genreName, PageRequest pageRequest);
    Page<Manga> findByGenresGenreNameContainingIgnoreCase(String genreName, Pageable pageable);
    Page<Manga> findByReleaseYear(String year, Pageable pageable);
    Page<Manga> findByMangaNameContainingIgnoreCase(String mangaName,Pageable pageable);
    Page<Manga> findByMangaStatusContainingIgnoreCase(String mangaName,Pageable pageable);
    Manga findByMangaName(String mangaName);
    List<Manga> findByAuthor(Author author);
    Page<Manga> findAll(Pageable pageable);
    @Query("SELECT m FROM Manga m ORDER BY m.id DESC")
    List<Manga> findMangasByIdDesc();
    List<Manga> findByGenresIn(Set<Genre> genres);
    List<Manga> findByMangaNameStartingWith(String query);
    List<Manga> findByRelatedMangasId(Long mangaId);
}