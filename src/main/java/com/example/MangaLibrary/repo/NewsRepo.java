package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NewsRepo extends CrudRepository<News, Long> {
    Page<News> findAll(Pageable pageable);
}
