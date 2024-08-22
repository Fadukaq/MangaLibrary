package com.example.MangaLibrary.repo;

import com.example.MangaLibrary.models.News;
import com.example.MangaLibrary.models.NewsRating;
import com.example.MangaLibrary.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NewsRatingRepo extends CrudRepository<NewsRating, Long> {

    Optional<NewsRating> findByNewsAndUser(News news, User user);

    long countByNewsAndDelta(News news, int i);
}
