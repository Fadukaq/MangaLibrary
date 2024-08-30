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
    ChapterService chapterService;
    @Autowired
    CommentService commentService;
    @Autowired
    ChapterRepo chapterRepo;
    @Autowired
    CommentRepo commentRepo;

    //MANGA
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

        model.addAttribute("authors", authors);
        model.addAttribute("genres", genres);
        return "manga/manga-add";
    }

    @PostMapping("/manga/add")
    public String addManga(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult bindingResult, Model model) throws IOException {
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();

        model.addAttribute("maxYear", maxYear);
        model.addAttribute("genres", genres);
        model.addAttribute("authors", authors);

        if (!mangaService.isValidAddMangaForm(mangaForm, bindingResult)) {
            return "manga/manga-add";
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);
            mangaService.saveManga(mangaForm, user);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("manga.mangaName", "error.manga", e.getMessage());
            return "manga/manga-add";
        }

        return "redirect:/manga";
    }

    @GetMapping("/manga/edit/{id}")
    public String mangaEdit(@PathVariable("id") long id, Model model) {
        Optional<Manga> optionalManga = mangaRepo.findById(id);
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        List<Author> authors = authorRepo.findAll();
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            MangaForm mangaForm = new MangaForm();
            mangaForm.setManga(manga);

            model.addAttribute("manga", manga);
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
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
                                  BindingResult bindingResult, Model model) {
        List<Long> genreIds = mangaForm.getManga().getGenres().stream()
                .map(Genre::getId)
                .toList();

        if(!mangaService.isValidUpdateMangaForm(mangaForm, bindingResult)) {
            int maxYear = Year.now().getValue();
            List<Genre> genres = genreRepo.findAll();
            List<Author> authors = authorRepo.findAll();

            model.addAttribute("id", id);
            model.addAttribute("manga", mangaForm.getManga());
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
            model.addAttribute("mangaForm", mangaForm);
            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }
            return "manga/manga-edit";
        }
        mangaService.updateManga(id,mangaForm);
        return "redirect:/manga/"+id;
    }

    @GetMapping("/manga/delete/{id}")
    public String MangaDelete(@PathVariable(value ="id") long id,Model model) {
        Optional<Manga> mangaToDelete = mangaRepo.findById(id);
        if(mangaToDelete.isPresent()) {
            mangaRepo.delete(mangaToDelete.get());
            mangaService.deleteFolder(mangaToDelete.get().getId());
            Iterable<User> usersIterable = userRepo.findAll();
            List<User> users = new ArrayList<>();
            usersIterable.forEach(users::add);
            userService.deleteMangaFromUsersList(users, id);
            return "redirect:/manga";
        }else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }
    @PostMapping("/manga/delete/{id}")
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

    //Chapter
    @GetMapping("/manga/{mangaId}/chapter/add")
    public String chapterAddGet(@PathVariable Long mangaId, ChapterForm chapterForm, Model model) {
        Optional<Manga> manga = mangaRepo.findById(mangaId);
        if(manga.isPresent()) {
            chapterForm.setManga(manga.get());
            model.addAttribute("chapterForm", chapterForm);
            model.addAttribute("mangaId", mangaId);
            return "manga/manga-add-chapter";
        }else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }

    @PostMapping("/manga/{mangaId}/chapter/add")
    public String chapterAddPost(@PathVariable Long mangaId, @Valid @ModelAttribute ChapterForm chapterForm,
                                 BindingResult result, RedirectAttributes redirectAttributes, Model model) throws IOException {
        if (!chapterService.isValidChapterFormAdd(chapterForm, result)) {
            model.addAttribute("mangaId", mangaId);
            return "manga/manga-add-chapter";
        }

        Optional<Manga> thisManga = mangaRepo.findById(mangaId);
        if (thisManga.isPresent()) {
            Manga manga = thisManga.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepo.findByUserName(username);

            chapterService.addChapter(chapterForm, manga, user);
            return "redirect:/manga/" + mangaId;
        } else {
            model.addAttribute("errorMessage", "Такої манги не знайдено!");
            return "main/error";
        }
    }

    @GetMapping("/manga/{mangaId}/chapter/{chapterId}")
    public String viewChapter(@PathVariable Long mangaId, @PathVariable Long chapterId, Model model) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isPresent() && chapterOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = chapterOptional.get();
            String[] chapterImageFileNames = chapter.getChapterPages().split(",");

            String[] chapterImageUrls = new String[chapterImageFileNames.length];
            for (int i = 0; i < chapterImageFileNames.length; i++) {
                chapterImageUrls[i] = String.format("/images/mangas/%s/chapters/%s/%s", mangaId, chapter.getId(), chapterImageFileNames[i]);
            }

            model.addAttribute("manga", manga);
            model.addAttribute("chapter", chapter);
            model.addAttribute("chapterImageUrls", chapterImageUrls);

            return "manga/manga-chapter-view";
        } else {
            model.addAttribute("errorMessage", "Такої манги або глави не знайдено!");
            return "main/error";
        }
    }

    @GetMapping("/manga/{mangaId}/chapter/edit/{chapterId}")
    public String chapterEditGet(@PathVariable Long mangaId, @PathVariable Long chapterId, Model model) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isPresent() && chapterOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = chapterOptional.get();
            String[] chapterImageFileNames = chapter.getChapterPages().split(",");

            String[] chapterImageUrls = new String[chapterImageFileNames.length];
            for (int i = 0; i < chapterImageFileNames.length; i++) {
                chapterImageUrls[i] = String.format("/images/mangas/%d/chapters/%d/%s", mangaId, chapterId, chapterImageFileNames[i]);
            }

            ChapterForm chapterForm = new ChapterForm();
            chapterForm.setManga(manga);
            chapterForm.setChapter(chapter);
            model.addAttribute("chapterForm", chapterForm);
            model.addAttribute("manga", manga);
            model.addAttribute("chapter", chapter);
            model.addAttribute("chapterImageUrls", chapterImageUrls);
            return "manga/manga-chapter-edit";
        } else {
            model.addAttribute("errorMessage", "Такої глави, або манги не знайдено!");
            return "main/error";
        }
    }

    @PostMapping("/manga/{mangaId}/chapter/edit/{chapterId}")
    public String chapterEditPost(@PathVariable Long mangaId, @PathVariable Long chapterId,
                                  @Valid @ModelAttribute("chapterForm") ChapterForm chapterForm,
                                  BindingResult result, RedirectAttributes redirectAttributes,
                                  Model model) throws IOException {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isEmpty() || chapterOptional.isEmpty()) {
            model.addAttribute("errorMessage", "Такої глави, або манги не знайдено!");
            return "main/error";
        }

        Manga manga = mangaOptional.get();
        Chapter chapter = chapterOptional.get();

        if (!chapterService.isValidChapterForm(chapterForm , result)) {
            chapterService.addChapterDataToModel(model, manga, chapter, chapterForm);
            return "manga/manga-chapter-edit";
        }

        try {
            chapterService.editChapter(chapterForm, manga, chapter);
            redirectAttributes.addFlashAttribute("message", "Главу успішно оновлено");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Помилка під час оновлення глави: " + e.getMessage());
            return "main/error";
        }

        return "redirect:/manga/"+mangaId;
    }

    @PostMapping("/manga/{mangaId}/chapter/delete/{chapterId}")
    public String deleteChapter(@PathVariable Long mangaId, @PathVariable Long chapterId, RedirectAttributes redirectAttributes) {
        try {
            chapterService.deleteChapter(mangaId, chapterId);
            redirectAttributes.addFlashAttribute("message", "Главу успішно видалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка видалення глави: " + e.getMessage());
        }
        return "redirect:/manga/"+mangaId+"#chapters";
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
    public ResponseEntity<List<Manga>> searchManga(@RequestParam(value = "q", defaultValue = "") String query) {
        try {
            if (query.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<Manga> results = mangaRepo.findByMangaNameStartingWith(query);
            return ResponseEntity.ok(results);
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

    @PostMapping("/manga/{mangaId}/add-comment")
    @ResponseBody
    public ResponseEntity<Comment> addComment(@PathVariable Long mangaId, @RequestParam String text, Principal principal) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        if (!mangaOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Manga manga = mangaOptional.get();

        User user = userRepo.findByUserName(principal.getName());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (text == null || text.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Comment comment = new Comment(text, manga, user, LocalDateTime.now());
        commentRepo.save(comment);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/manga/{mangaId}/comments")
    public @ResponseBody Map<String, Object> getComments(
            @PathVariable Long mangaId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "4") int size,
            @RequestParam(required = false, defaultValue = "byNew") String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if ("byRating".equals(sortBy)) {
            pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "rating"));
        }
        Page<Comment> commentPage = commentRepo.findByMangaId(mangaId, pageable);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        Map<Long, Integer> userRatings = new HashMap<>();
        Map<Long, Map<String, Integer>> commentRatings = new HashMap<>();
        Map<Long, List<Replies>> commentReplies = new HashMap<>();

        for (Comment comment : commentPage.getContent()) {
            CommentRating userRating = commentRatingRepo.findByCommentIdAndUserId(comment.getId(), user.getId());
            userRatings.put(comment.getId(), userRating != null ? userRating.getDelta() : 0);

            long upvotes = commentRatingRepo.countByCommentIdAndDelta(comment.getId(), 1);
            long downvotes = commentRatingRepo.countByCommentIdAndDelta(comment.getId(), -1);

            Map<String, Integer> ratingInfo = new HashMap<>();
            ratingInfo.put("upvotes", (int) upvotes);
            ratingInfo.put("downvotes", (int) downvotes);
            commentRatings.put(comment.getId(), ratingInfo);

            List<Replies> replies = repliesRepo.findByParentCommentId(comment.getId());
            commentReplies.put(comment.getId(), replies);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("comments", commentPage.getContent());
        response.put("userRatings", userRatings);
        response.put("commentRatings", commentRatings);
        response.put("commentReplies", commentReplies);
        response.put("hasMore", commentPage.hasNext());

        return response;
    }

    @GetMapping("/manga/comment/{commentId}/edit")
    public @ResponseBody ResponseEntity<?> editComment(@PathVariable Long commentId, @RequestParam String commentText, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findByUserName(userDetails.getUsername());

        return commentRepo.findById(commentId)
                .map(comment -> {
                    if (comment.getUser().getId() != currentUser.getId()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    if (commentText.isEmpty() || commentText.length() > 1000) {
                        return ResponseEntity.badRequest().body("Коментар повинен містити від 1 до 1000 символів");
                    }
                    comment.setText(commentText);
                    commentRepo.save(comment);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/manga/comment/{commentId}/delete")
    public @ResponseBody ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findByUserName(userDetails.getUsername());

        return commentRepo.findById(commentId)
                .map(comment -> {
                    if (comment.getUser().getId() != currentUser.getId()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    commentRepo.delete(comment);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/manga/comment/{commentId}/rate")
    public ResponseEntity<Map<String, Object>> updateRatingGET(@PathVariable Long commentId,
                                                               @RequestParam Long userId,
                                                               @RequestParam int delta) {
        commentService.updateRating(commentId, userId, delta);

        Map<String, Object> ratingInfo = new HashMap<>();
        int upvotes = commentRatingRepo.getCountByCommentIdAndDelta(commentId, 1);
        int downvotes = commentRatingRepo.getCountByCommentIdAndDelta(commentId, -1);
        int newRatingScore = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"))
                .getRating();

        String ratingTitle = "Плюсів: " + upvotes + " | Мінусів: " + downvotes;

        ratingInfo.put("newRatingScore", newRatingScore);
        ratingInfo.put("ratingTitle", ratingTitle);
        return ResponseEntity.ok(ratingInfo);
    }
    @GetMapping("/manga/comment/{commentId}/report")
    public ResponseEntity<String> reportComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestParam String reason) {

        boolean success = commentService.reportComment(commentId, userId, reason);
        if (success) {
            return ResponseEntity.ok("Comment reported successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have already reported this comment");
        }
    }

    @GetMapping("/manga/comment/reply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReply(
            @RequestParam("text") String text,
            @RequestParam("parentCommentId") Long parentCommentId,
            @RequestParam("mangaId") Long mangaId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Comment parentComment = commentRepo.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = userRepo.findByUserName(userDetails.getUsername());

            String mentionedUsername = null;
            Pattern pattern = Pattern.compile("@(\\w+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                mentionedUsername = matcher.group(1);
            }
            User userReplied = null;
            if (mentionedUsername != null) {
                userReplied = userRepo.findByUserName(mentionedUsername);
                if (userReplied != null) {
                    text = text.replace("@" + mentionedUsername, "@" + userReplied.getId());
                }
            }

            Replies reply = new Replies();
            reply.setText(text);
            reply.setParentComment(parentComment);
            reply.setUser(userRepo.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
            reply.setCreatedAt(LocalDateTime.now());
            reply.setManga(mangaRepo.findById(mangaId)
                    .orElseThrow(() -> new RuntimeException("Manga not found")));
            repliesRepo.save(reply);

            response.put("success", true);
            response.put("reply", reply);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при добавлении ответа.");
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
    @GetMapping("/manga/reply/{replyId}/edit")
    @ResponseBody
    public ResponseEntity<?> editReply(
            @PathVariable Long replyId,
            @RequestParam String text,
            Principal principal) {

        try {
            String currentUsername = principal.getName();
            commentService.updateReply(replyId, text, currentUsername);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating reply");
        }
    }
    @GetMapping("/manga/reply/{replyId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteReply(
            @PathVariable Long replyId,
            Principal principal) {
        try {
            String currentUsername = principal.getName();
            commentService.deleteReply(replyId, currentUsername);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting reply");
        }
    }
    @GetMapping("/manga/reply/{replyId}/report")
    public ResponseEntity<String> reportReply(
            @PathVariable Long replyId,
            @RequestParam Long userId,
            @RequestParam String reason) {
        boolean success = commentService.reportReply(replyId, userId, reason);
        if (success) {
            return ResponseEntity.ok("Reply reported successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You have already reported this reply");
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
