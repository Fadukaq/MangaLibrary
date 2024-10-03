package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.manga.ChapterForm;
import com.example.MangaLibrary.helper.manga.ChapterImage;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import com.example.MangaLibrary.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Controller
public class MangaController {
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private GenreRepo genreRepo;
    @Autowired
    private UserSettingsRepo userSettingsRepo;
    @Autowired
    private CommentRatingRepo commentRatingRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AuthorRepo authorRepo;
    @Autowired
    private RatingRepo ratingRepo;
    @Autowired
    private RepliesRepo repliesRepo;
    @Autowired
    private MangaLibraryManager mangaLibraryManager;
    private static final int PAGE_SIZE = 25;
    @Autowired
    UserService userService;
    @Autowired
    MangaService mangaService;
    @Autowired
    NotificationRepo notificationRepo;
    @Autowired
    ChapterService chapterService;
    @Autowired
    CommentService commentService;
    @Autowired
    ChapterRepo chapterRepo;
    @Autowired
    ReplyService replyService;
    @Autowired
    CommentRepo commentRepo;
    @GetMapping("/manga")
    public String mangas(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "byNew") String sortOrder,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            Model model,
            HttpServletRequest request
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable;

        if ("byRating".equals(sortOrder)) {
            pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(sortDirection, "averageRating"));
        } else {
            pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(sortDirection, "id"));
        }

        Page<Manga> mangaPage = mangaRepo.findAll(pageable);
        List<Manga> mangaList = mangaPage.getContent();
        int totalPages = mangaPage.getTotalPages();

        if (page > totalPages) {
            return "redirect:/manga?page=" + totalPages;
        }
        model.addAttribute("mangas", mangaList);
        model.addAttribute("page", mangaPage);
        if (request.getHeader("X-Requested-With") != null && request.getHeader("X-Requested-With").equals("XMLHttpRequest")) {
            return "manga/manga-partial :: manga-content";
        }
        return "manga/manga-main";
    }

    @GetMapping("/manga/add")
    public String mangaAdd(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "manga/manga-add";
        }
        int maxYear = Year.now().getValue();
        model.addAttribute("maxYear", maxYear);
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();
        List<Manga> mangas = (List<Manga>) mangaRepo.findAll();

        model.addAttribute("mangas", mangas);
        model.addAttribute("authors", authors);
        model.addAttribute("genres", genres);
        return "manga/manga-add";
    }

    @PostMapping("/manga/add")
    public String addManga(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult bindingResult,
                           @RequestParam(required = false) List<Long> relatedMangaIds,
                           Model model) throws IOException {
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();
        List<Manga> mangas = (List<Manga>) mangaRepo.findAll();
        model.addAttribute("maxYear", maxYear);
        model.addAttribute("genres", genres);
        model.addAttribute("authors", authors);
        model.addAttribute("mangas", mangas);

        if (!mangaService.isValidAddMangaForm(mangaForm, bindingResult)) {
            return "manga/manga-add";
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);
            System.out.println("POSTER:" + mangaForm.getManga().getMangaPosterImg());
            mangaService.saveManga(mangaForm, relatedMangaIds, user);
            return "redirect:/manga";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("manga.mangaName", "error.manga", e.getMessage());
            return "manga/manga-add";
        }
    }

    @GetMapping("/manga/edit/{id}")
    public String mangaEdit(@PathVariable("id") long id, Model model) {
        Optional<Manga> optionalManga = mangaRepo.findById(id);
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();
        List<Manga> mangas = (List<Manga>) mangaRepo.findAll();
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            MangaForm mangaForm = new MangaForm();
            mangaForm.setManga(manga);
            List<Long> relatedMangaIds = manga.getRelatedMangas().stream()
                    .map(Manga::getId)
                    .collect(Collectors.toList());

            model.addAttribute("manga", manga);
            model.addAttribute("mangas", mangas);
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
            model.addAttribute("relatedMangaIds", relatedMangaIds);
            model.addAttribute("mangaForm", mangaForm);
            model.addAttribute("posterImageUrl", manga.getMangaPosterImg());
            model.addAttribute("backGroundImageUrl", manga.getMangaBackGround());
            return "manga/manga-edit";
        } else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }
    @PostMapping("/manga/edit/{id}")
    public String mangaPostUpdate(@PathVariable("id") long id,
                                  @ModelAttribute("mangaForm") @Valid MangaForm mangaForm,
                                  BindingResult bindingResult,
                                  @RequestParam(required = false) List<Long> relatedMangaIds,
                                  Model model) {
        List<Long> genreIds = mangaForm.getManga().getGenres().stream()
                .map(Genre::getId)
                .toList();

        if(!mangaService.isValidUpdateMangaForm(mangaForm, bindingResult)) {
            int maxYear = Year.now().getValue();
            List<Genre> genres = genreRepo.findAll();
            List<Author> authors = authorRepo.findAll();
            List<Manga> mangas = (List<Manga>) mangaRepo.findAll();

            model.addAttribute("id", id);
            model.addAttribute("manga", mangaForm.getManga());
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
            model.addAttribute("mangas", mangas);
            model.addAttribute("mangaForm", mangaForm);
            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }
            return "manga/manga-edit";
        }
        mangaService.updateManga(id, mangaForm, relatedMangaIds);
        return "redirect:/manga/"+id;
    }

    @PostMapping("/manga/delete/{id}")
    public String deleteManga(@PathVariable("id") long id, Model model) {
        Optional<Manga> mangaToDelete = mangaRepo.findById(id);
        if (mangaToDelete.isPresent()) {
            mangaRepo.delete(mangaToDelete.get());
            mangaService.deleteFolder(mangaToDelete.get().getId());
            Iterable<User> usersIterable = userRepo.findAll();
            List<User> users = new ArrayList<>();
            usersIterable.forEach(users::add);
            userService.deleteMangaFromUsersList(users, id);
            return "redirect:/manga";
        } else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }

    @PostMapping("/manga/deleteByAdmin/{id}")
    public String mangaDelete(@PathVariable(value = "id") long id) {
            Optional<Manga> mangaToDelete = mangaRepo.findById(id);
            if (mangaToDelete.isPresent()) {
            mangaRepo.delete(mangaToDelete.get());
            mangaService.deleteFolder(mangaToDelete.get().getId());
            Iterable<User> usersIterable = userRepo.findAll();
            List<User> users = new ArrayList<>();
            usersIterable.forEach(users::add);
            userService.deleteMangaFromUsersList(users, id);
                return "redirect:/admin-dashboard?tab=mangaTable";
        } else {
            return "redirect:/error?message=Такої манги не знайдено!";
        }
    }

    @GetMapping("/manga/{id}")
    public String getManga(@PathVariable Long id, ModelMap model, Principal principal) {
        Optional<Manga> optionalManga = mangaRepo.findById(id);
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            List<Chapter> chapters = chapterRepo.findByMangaId(manga.getId());

            UserSettings userSettings = new UserSettings();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);

            userSettings = userSettingsRepo.findByUser(user);

            String translatedStatus = mangaService.getMangaTranslatedStatus(manga.getMangaStatus());

            Set<Manga> relatedMangasSet = mangaService.getRelatedMangas(id);
            Set<Long> relatedMangaIds = relatedMangasSet.stream()
                    .map(Manga::getId)
                    .collect(Collectors.toSet());

            List<Map<String, Object>> relatedMangasMap = relatedMangasSet.stream()
                    .map(relatedManga -> {
                        Map<String, Object> mangaMap = new HashMap<>();
                        mangaMap.put("id", relatedManga.getId());
                        mangaMap.put("mangaName", relatedManga.getMangaName());
                        mangaMap.put("mangaPosterImg", relatedManga.getMangaPosterImg());
                        mangaMap.put("averageRating", relatedManga.getAverageRating());

                        List<Map<String, Object>> genresMap = relatedManga.getGenres().stream()
                                .map(genre -> {
                                    Map<String, Object> genreMap = new HashMap<>();
                                    genreMap.put("id", genre.getId());
                                    genreMap.put("genreName", genre.getGenreName());
                                    return genreMap;
                                })
                                .collect(Collectors.toList());
                        mangaMap.put("genres", genresMap);

                        return mangaMap;
                    })
                    .collect(Collectors.toList());

            Set<Genre> genres = new HashSet<>(manga.getGenres());
            List<Manga> similarMangas = mangaRepo.findByGenresIn(genres);

            List<Map<String, Object>> similarMangasMap = similarMangas.stream()
                    .filter(similarManga -> similarManga.getId() != manga.getId())
                    .filter(similarManga -> !relatedMangaIds.contains(similarManga.getId()))
                    .filter(similarManga -> {
                        Set<Genre> commonGenres = new HashSet<>(similarManga.getGenres());
                        commonGenres.retainAll(genres);
                        return commonGenres.size() >= 4;
                    })
                    .limit(8)
                    .map(similarManga -> {
                        Map<String, Object> mangaMap = new HashMap<>();
                        mangaMap.put("id", similarManga.getId());
                        mangaMap.put("mangaName", similarManga.getMangaName());
                        mangaMap.put("mangaPosterImg", similarManga.getMangaPosterImg());

                        mangaMap.put("averageRating", similarManga.getAverageRating());

                        List<Map<String, Object>> chaptersMap = similarManga.getChapter().stream()
                                .map(chapter -> {
                                    Map<String, Object> chapterMap = new HashMap<>();
                                    chapterMap.put("id", chapter.getId());
                                    chapterMap.put("title", chapter.getTitle());
                                    return chapterMap;
                                })
                                .collect(Collectors.toList());
                        mangaMap.put("chapters", chaptersMap);

                        List<Map<String, Object>> genresMap = similarManga.getGenres().stream()
                                .map(genre -> {
                                    Map<String, Object> genreMap = new HashMap<>();
                                    genreMap.put("id", genre.getId());
                                    genreMap.put("genreName", genre.getGenreName());
                                    return genreMap;
                                })
                                .collect(Collectors.toList());
                        mangaMap.put("genres", genresMap);

                        mangaMap.put("mangaStatus", similarManga.getMangaStatus());
                        mangaMap.put("description", similarManga.getMangaDescription());

                        return mangaMap;
                    }).collect(Collectors.toList());

            boolean isFavorited = user != null && user.getMangaFavorites().contains(String.valueOf(id));
            MangaService.addMangaStatusAttributes(user, id, model);

            Optional<Rating> ratingOptional = ratingRepo.findByMangaAndUser(manga,user);
            if(ratingOptional.isPresent()){
                Rating ratingValue = ratingOptional.get();
                model.addAttribute("ratingValue", ratingValue.getRating());
            }
            List<Comment> comments = commentRepo.findByMangaIdOrderByCreatedAtDesc(id);

            Map<Long, Integer> userRatings = new HashMap<>();
            Map<Long, Map<String, Integer>> commentRatings = new HashMap<>();
            Map<Long, List<Replies>> commentReplies = new HashMap<>();

            for (Comment comment : comments) {
                CommentRating userRating = commentRatingRepo.findByCommentIdAndUserId(comment.getId(), user.getId());
                userRatings.put(comment.getId(), userRating != null ? userRating.getDelta() : 0);

                long upvotes = commentRatingRepo.countByCommentIdAndDelta(comment.getId(), 1);
                long downvotes = commentRatingRepo.countByCommentIdAndDelta(comment.getId(), -1);

                Map<String, Integer> ratingInfo = new HashMap<>();
                ratingInfo.put("upvotes", (int) upvotes);
                ratingInfo.put("downvotes", (int) downvotes);
                commentRatings.put(comment.getId(), ratingInfo);

                List<Replies> replies = repliesRepo.findByParentCommentIdOrderByCreatedAtDesc(comment.getId());
                for (Replies reply : replies) {
                    reply.setText(replyService.convertUserIdsToUsernames(reply.getText()));
                }
                commentReplies.put(comment.getId(), replies);
            }



            model.addAttribute("comments", comments);
            model.addAttribute("userRatings", userRatings);
            model.addAttribute("commentRatings", commentRatings);
            model.addAttribute("commentReplies", commentReplies);

            model.addAttribute("manga", manga);
            model.addAttribute("user", user);
            model.addAttribute("translatedMangaStatus", translatedStatus);
            model.addAttribute("userSettings", userSettings);
            model.addAttribute("chapters", chapters);
            model.addAttribute("chapterCount", chapters.size());
            model.addAttribute("similarMangas", similarMangasMap);
            model.addAttribute("relatedMangas", relatedMangasMap);

            model.addAttribute("countReading", mangaService.getCountByReading(String.valueOf(id)));
            model.addAttribute("countWantToRead", mangaService.getCountByWantToRead(String.valueOf(id)));
            model.addAttribute("countStoppedReading", mangaService.getCountByStoppedReading(String.valueOf(id)));
            model.addAttribute("countRecited", mangaService.getCountByRecited(String.valueOf(id)));
            model.addAttribute("countFavorites", mangaService.getCountByFavorites(String.valueOf(id)));

            model.addAttribute("countOneStar", mangaService.getCountByOneStar(String.valueOf(id)));
            model.addAttribute("countTwoStar", mangaService.getCountByTwoStar(String.valueOf(id)));
            model.addAttribute("countThreeStar", mangaService.getCountByThreeStar(String.valueOf(id)));
            model.addAttribute("countFourStar", mangaService.getCountByFourStar(String.valueOf(id)));
            model.addAttribute("countFiveStar", mangaService.getCountByFiveStar(String.valueOf(id)));

            model.addAttribute("isFavorited", isFavorited);
            return "manga/manga-details";
        } else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }

    @PostMapping("/manga/add-to-list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addMangaToList(@RequestParam("listType") String listType,
                                                                @RequestParam("mangaId") Long mangaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);
        Map<String, Object> response = new HashMap<>();
        if(user != null) {
            userService.deleteMangaFromUserList(user, mangaId);
            userService.addMangaToList(user, listType, mangaId);
            userRepo.save(user);
            response.put("success", true);
            response.put("message", "Манга успішно додана у список");
        } else {
            response.put("success", false);
            response.put("message", "Користувач не знайдений");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/manga/add-to-favorites")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestParam("mangaId") Long mangaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            List<String> favorites = user.getMangaFavorites();

            if (favorites.contains(String.valueOf(mangaId))) {
                favorites.remove(String.valueOf(mangaId));
                response.put("message", "Манга успішно видалена з обраного");
            } else {
                favorites.add(String.valueOf(mangaId));
                response.put("message", "Манга успішно додана в обране");
            }

            userRepo.save(user);
            response.put("success", true);
        } else {
            response.put("success", false);
            response.put("message", "Користувач не знайдений");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<MangaDTO>> searchManga(@RequestParam(value = "q", defaultValue = "") String query) {
        try {
            if (query.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<Manga> results = mangaRepo.findByMangaNameStartingWith(query);

            List<MangaDTO> mangaDTOs = results.stream()
                    .map(manga -> {
                        MangaDTO dto = new MangaDTO();
                        dto.setId(manga.getId());
                        dto.setMangaName(manga.getMangaName());
                        dto.setMangaDescription(manga.getMangaDescription());
                        dto.setMangaStatus(manga.getMangaStatus());
                        dto.setMangaPosterImg(manga.getMangaPosterImg());
                        dto.setMangaBackGround(manga.getMangaBackGround());
                        dto.setReleaseYear(manga.getReleaseYear());
                        dto.setAdultContent(manga.getAdultContent());
                        dto.setAverageRating(manga.getAverageRating());
                        dto.setTotalRatings(manga.getTotalRatings());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(mangaDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/rate-manga")
    @ResponseBody
    ResponseEntity<Map<String, Object>> rateManga(@RequestParam("mangaId") Long mangaId,
                                                    @RequestParam("userId") Long userId,
                                                    @RequestParam("rating") int rating) {
        Map<String, Object> response = new HashMap<>();
        try {
            mangaService.saveRating(mangaId, userId, rating);
            response.put("success", true);
            response.put("message", "Ви успішно додали оцінку!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Виникла помилка при збереженні оцінки.");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove-rating")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeRating(@RequestParam("mangaId") Long mangaId,
                                                            @RequestParam("userId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            mangaService.removeRating(mangaId, userId);
            response.put("success", true);
            response.put("message", "Оценка успешно удалена!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при удалении оценки: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manga/getUsernameById")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getUsernameById(@RequestParam("userId") Long userId) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            response.put("username", user.getUserName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/random")
    public String getRandomMangaId(Model model) {

        Long randomMangaId = mangaService.getRandomMangaId();

        model.addAttribute("randomMangaId", randomMangaId);
        if(randomMangaId==null) {
            return "redirect:/manga";
        }
        return "redirect:/manga/" + randomMangaId;
    }
    @GetMapping("/filter-manga")
    public String filterManga(
            @RequestParam(value = "genre", required = false) List<Long> genreIds,
            @RequestParam(value = "author", required = false) List<Long> authorIds,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "ageRating", required = false) Boolean ageRating,
            @RequestParam(value = "yearFrom", required = false) Integer yearFrom,
            @RequestParam(value = "yearTo", required = false) Integer yearTo,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "sort", defaultValue = "releaseYear") String sortOrder,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            Model model,
            ModelMap modelMap
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        String sortField = "releaseYear";
        if ("byNew".equalsIgnoreCase(sortOrder)) {
            sortField = "releaseYear";
        } else if ("byTitle".equalsIgnoreCase(sortOrder)) {
            sortField = "title";
        }

        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by(sortDirection, sortField));

        String effectiveStatus = "all".equals(status) ? null : status;
        Page<Manga> mangaPage = mangaRepo.findFiltered(
                genreIds,
                authorIds,
                effectiveStatus,
                ageRating,
                yearFrom,
                yearTo,
                pageable
        );
        List<Manga> mangaList = mangaPage.getContent();
        model.addAttribute("mangas", mangaList);
        modelMap.addAttribute("mangasCount", mangaPage.getTotalElements());
        model.addAttribute("page", mangaPage);
        return "manga/manga-partial :: manga-content";
    }
}
