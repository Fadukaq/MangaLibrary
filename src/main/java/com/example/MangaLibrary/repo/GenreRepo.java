package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Genre;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GenreRepo extends CrudRepository<Genre, Long>{
    Genre findByGenreName(String genreName);
    List<Genre> findAll();
    List<Genre> findAllByOrderByIdDesc();


}
