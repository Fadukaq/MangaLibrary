package com.example.MangaLibrary.service;

import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.repo.ChapterRepo;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MainService {
    private final MangaRepo mangaRepo;
    private final ChapterRepo chapterRepo;
    private final GenreRepo genreRepo;

    @Autowired
    MangaService mangaService;
    @Autowired
    public MainService(MangaRepo mangaRepo, ChapterRepo chapterRepo, GenreRepo genreRepo) {
        this.mangaRepo = mangaRepo;
        this.chapterRepo = chapterRepo;
        this.genreRepo = genreRepo;
    }

    public List<Map<String, Object>> getLatestMangaWithChapters(int maxUniqueMangas) {
        Set<Long> uniqueMangaIds = new HashSet<>();

        return chapterRepo.findLatestChapters(PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .filter(chapter -> uniqueMangaIds.add(chapter.getManga().getId()))
                .limit(maxUniqueMangas)
                .map(chapter -> {
                    Manga manga = chapter.getManga();
                    Map<String, Object> mangaInfo = new HashMap<>();
                    mangaInfo.put("mangaId", manga.getId());
                    mangaInfo.put("mangaName", manga.getMangaName());
                    mangaInfo.put("mangaPosterImg", manga.getMangaPosterImg());
                    mangaInfo.put("chapterId", chapter.getId());
                    mangaInfo.put("chapterTitle", chapter.getTitle());
                    mangaInfo.put("creationTime", chapter.getCreationTime());
                    return mangaInfo;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewMangaList(int limit) {
        return mangaRepo.findMangasByIdDesc().stream()
                .limit(limit)
                .map(manga -> {
                    Map<String, Object> mangaMap = new HashMap<>();
                    mangaMap.put("id", manga.getId());
                    mangaMap.put("mangaName", manga.getMangaName());
                    mangaMap.put("mangaPosterImg", manga.getMangaPosterImg());

                    mangaMap.put("chapters", manga.getChapter().stream()
                            .map(chapter -> {
                                Map<String, Object> chapterMap = new HashMap<>();
                                chapterMap.put("id", chapter.getId());
                                chapterMap.put("chapterTitle", chapter.getTitle());
                                return chapterMap;
                            })
                            .collect(Collectors.toList()));

                    mangaMap.put("genres", manga.getGenres().stream()
                            .map(genre -> {
                                Map<String, Object> genreMap = new HashMap<>();
                                genreMap.put("id", genre.getId());
                                genreMap.put("name", genre.getGenreName());
                                return genreMap;
                            })
                            .collect(Collectors.toList()));

                    return mangaMap;
                })
                .collect(Collectors.toList());
    }

    public Map<String, List<Manga>> getMangaByGenre(int maxGenres, int maxResults, int minResults) {
        List<Genre> allGenres = genreRepo.findAll();
        Map<String, List<Manga>> mangaByGenre = new LinkedHashMap<>();
        Set<Long> addedMangaIds = new HashSet<>();

        for (Genre genre : allGenres) {
            if (mangaByGenre.size() >= maxGenres) {
                break;
            }
            List<Manga> uniqueMangaList = mangaRepo.findMangasByGenreName(genre.getGenreName(), PageRequest.of(0, maxResults * 3).withSort(Sort.by(Sort.Order.desc("id"))))
                    .stream()
                    .filter(manga -> addedMangaIds.add(manga.getId()))
                    .limit(maxResults)
                    .collect(Collectors.toList());

            if (uniqueMangaList.size() >= minResults) {
                mangaByGenre.put(genre.getGenreName(), uniqueMangaList);
            }
        }

        for (Genre genre : allGenres) {
            if (mangaByGenre.size() >= maxGenres) {
                break;
            }
            if (!mangaByGenre.containsKey(genre.getGenreName())) {
                List<Manga> uniqueMangaList = mangaRepo.findMangasByGenreName(genre.getGenreName(), PageRequest.of(0, maxResults).withSort(Sort.by(Sort.Order.desc("id"))))
                        .stream()
                        .filter(manga -> addedMangaIds.add(manga.getId()))
                        .collect(Collectors.toList());
                if (!uniqueMangaList.isEmpty()) {
                    mangaByGenre.put(genre.getGenreName(), uniqueMangaList);
                }
            }
        }

        for (Map.Entry<String, List<Manga>> entry : mangaByGenre.entrySet()) {
            for (Manga manga : entry.getValue()) {
                manga.setMangaStatus(mangaService.getMangaTranslatedStatus(manga.getMangaStatus()));
            }
        }

        return mangaByGenre;
    }
}
