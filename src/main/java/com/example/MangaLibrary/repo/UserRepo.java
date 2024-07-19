package com.example.MangaLibrary.repo;


import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepo extends CrudRepository<User, Long> {
    List<User> findAll();
    User findByUserName(String userName);

    User findByVerificationToken(String code);

    User findByEmail(String email);

    List<User> findByUserNameContaining(String username);
}
