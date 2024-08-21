package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.News;
import org.springframework.data.repository.CrudRepository;

public interface NewsRepo extends CrudRepository<News, Long> {

}
