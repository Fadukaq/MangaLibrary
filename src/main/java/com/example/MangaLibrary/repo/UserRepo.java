package com.example.MangaLibrary.repo;


import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface UserRepo extends CrudRepository<User, Long> {
    List<User> findAll();
    User findByUserName(String userName);

    User findByVerificationToken(String code);

    User findByEmail(String email);

    List<User> findByUserNameContaining(String username);
    @Query("SELECT COUNT(u) FROM User u WHERE :mangaId MEMBER OF u.mangaReading")
    long countByMangaReading(@Param("mangaId") String mangaId);

    @Query("SELECT COUNT(u) FROM User u WHERE :mangaId MEMBER OF u.mangaWantToRead")
    long countByMangaWantToRead(@Param("mangaId") String mangaId);

    @Query("SELECT COUNT(u) FROM User u WHERE :mangaId MEMBER OF u.mangaStoppedReading")
    long countByMangaStoppedReading(@Param("mangaId") String mangaId);

    @Query("SELECT COUNT(u) FROM User u WHERE :mangaId MEMBER OF u.mangaRecited")
    long countByMangaRecited(@Param("mangaId") String mangaId);

    @Query("SELECT COUNT(u) FROM User u WHERE :mangaId MEMBER OF u.mangaFavorites")
    long countByMangaFavorites(@Param("mangaId") String mangaId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.manga.id = :mangaId AND r.rating = 1")
    long countByMangaOneStar(@Param("mangaId") Long mangaId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.manga.id = :mangaId AND r.rating = 2")
    long countByMangaTwoStar(@Param("mangaId") Long mangaId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.manga.id = :mangaId AND r.rating = 3")
    long countByMangaThreeStar(@Param("mangaId") Long mangaId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.manga.id = :mangaId AND r.rating = 4")
    long countByMangaFourStar(@Param("mangaId") Long mangaId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.manga.id = :mangaId AND r.rating = 5")
    long countByMangaFiveStar(@Param("mangaId") Long mangaId);
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM User u JOIN u.friends f " +
            "WHERE u = :user AND f = :friend")
    boolean getIsFriends(@Param("user") User user, @Param("friend") User friend);

    boolean existsByEmail(String newEmail);
}
