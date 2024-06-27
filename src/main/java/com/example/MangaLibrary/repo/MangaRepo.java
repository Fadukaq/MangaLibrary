package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Author;
import com.example.MangaLibrary.models.Manga;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MangaRepo extends CrudRepository<Manga, Long> {
    @Query("SELECT m FROM Manga m JOIN m.genres g WHERE g.genreName = :genreName")
    Page<Manga> findAllByGenreNamePagination(@Param("genreName") String genreName, Pageable pageable);
    @Query("SELECT m FROM Manga m WHERE m.author.id = :authorId")
    Page<Manga> findAllByAuthorIdPagination(@Param("authorId") Long authorId, Pageable pageable);
    Page<Manga> findByGenresGenreNameContainingIgnoreCase(String genreName, Pageable pageable);
    Page<Manga> findByReleaseYear(String year, Pageable pageable);
    Page<Manga> findByMangaNameContainingIgnoreCase(String mangaName,Pageable pageable);
    Page<Manga> findByMangaStatusContainingIgnoreCase(String mangaName,Pageable pageable);
    Manga findByMangaName(String mangaName);
    List<Manga> findByAuthor(Author author);
    Page<Manga> findAll(Pageable pageable);
}