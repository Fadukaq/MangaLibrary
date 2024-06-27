package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepo extends JpaRepository<Author, Long> {
    List<Author> findAll();
    Author findByName(String name);
    Author findById(long id);
}
