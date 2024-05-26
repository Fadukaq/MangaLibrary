package com.example.MangaLibrary.repo;


import com.example.MangaLibrary.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<User, Long> {
    User findByUserName(String userName);
}
