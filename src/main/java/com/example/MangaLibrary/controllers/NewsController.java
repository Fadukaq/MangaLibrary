package com.example.MangaLibrary.controllers;

import com.example.MangaLibrary.models.News;
import com.example.MangaLibrary.models.NewsRating;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.NewsRatingRepo;
import com.example.MangaLibrary.repo.NewsRepo;
import com.example.MangaLibrary.repo.UserRepo;
import com.example.MangaLibrary.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class NewsController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private NewsRepo newsRepo;
    @Autowired
    private NewsRatingRepo newsRatingRepo;
    @Autowired
    private NewsService newsService;
    @GetMapping("/news")
    public String newsList(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageIndex = page - 1;

        Page<News> newsPage = newsRepo.findAll(PageRequest.of(pageIndex, 5, Sort.by("createdAt").descending()));

        if (pageIndex >= newsPage.getTotalPages() || pageIndex < 0) {
            model.addAttribute("errorMessage", "Така сторінка не знайдена.");
            return "main/error";
        }
        model.addAttribute("newsList", newsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("newsPage", newsPage);
        return "news/news-list";
    }
    @GetMapping("/news/{id}")
    public String newsDetails(@PathVariable Long id, Model model) {
        News news = newsRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid news ID: " + id));
        long positiveCount = newsRatingRepo.countByNewsAndDelta(news, 1);
        long negativeCount = newsRatingRepo.countByNewsAndDelta(news, -1);

        String positiveAndNegative = "Плюсів: "+positiveCount+" | Мінусів: "+negativeCount;
        model.addAttribute("positiveAndNegative", positiveAndNegative);
        model.addAttribute("news", news);
        return "news/news-details";
    }
    @GetMapping("/news/add")
    public String newsAddGet(Model model, Principal principal) {
        model.addAttribute("news", new News());
        return "news/news-add";
    }
    @PostMapping("/news/add")
    public String newsAddPost(@Valid News news, BindingResult result,Principal principal) {
        if (result.hasErrors()) {
            return "news/news-add";
        }
        String username = principal.getName();
        User currentUser = userRepo.findByUserName(username);
        news.setUser(currentUser);
        news.setCreatedAt(LocalDateTime.now());
        newsRepo.save(news);
        return "redirect:/news";
    }

    @GetMapping("/news/edit/{id}")
    public String newsEditGet(@PathVariable Long id, Model model) {
        Optional<News> news = newsRepo.findById(id);
        if(news.isPresent()){
            model.addAttribute("news", news.get());
            return "news/news-edit";
        } else {
            model.addAttribute("errorMessage", "Такої новини не знайдено!");
            return "main/error";
        }
    }
    @PostMapping("/news/edit/{id}")
    public String newsEditPost(@PathVariable Long id, @ModelAttribute("news") @Valid News news, BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("news", news);
            return "news/news-edit";
        }
        Optional<News> existingNews = newsRepo.findById(id);
        if(existingNews.isPresent()) {
            News currentNews = existingNews.get();
            currentNews.setTitle(news.getTitle());
            currentNews.setText(news.getText());
            newsRepo.save(currentNews);
            return "redirect:/news";
        } else {
            model.addAttribute("errorMessage", "Такої новини не знайдено!");
            return "main/error";
        }
    }
    @PostMapping("/news/delete/{id}")
    public String newsDeletePost(@PathVariable Long id, Model model) {
        Optional<News> existingNews = newsRepo.findById(id);
        if(existingNews.isPresent()) {
            newsRepo.delete(existingNews.get());
            return "redirect:/news";
        }else {
            model.addAttribute("errorMessage", "Такої новини не знайдено!");
            return "main/error";
        }
    }
    @PostMapping("/news/deleteByAdminDashBoard/{id}")
    public String newsDeletePostByAdminDashBoard(@PathVariable Long id, Model model) {
        Optional<News> existingNews = newsRepo.findById(id);
        if(existingNews.isPresent()) {
            newsRepo.delete(existingNews.get());
            return "redirect:/admin-dashboard?tab=newsTable";
        }else {
            model.addAttribute("errorMessage", "Такої новини не знайдено!");
            return "main/error";
        }
    }
    @GetMapping("/news/rate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rateNews(@RequestParam Long newsId, @RequestParam int delta, Principal principal) {
        News news = newsRepo.findById(newsId).orElseThrow(() -> new IllegalArgumentException("Invalid news ID: " + newsId));
        String username = principal.getName();
        User user = userRepo.findByUserName(username);

        Optional<NewsRating> existingRating = newsRatingRepo.findByNewsAndUser(news, user);
        int userRating;

        if (existingRating.isPresent()) {
            NewsRating rating = existingRating.get();
            int oldDelta = rating.getDelta();
            if (oldDelta == delta) {
                news.setRating(news.getRating() - oldDelta);
                newsRatingRepo.delete(rating);
                userRating = 0;
            } else {
                news.setRating(news.getRating() - oldDelta + delta);
                rating.setDelta(delta);
                newsRatingRepo.save(rating);
                userRating = delta;
            }
        } else {
            NewsRating newRating = new NewsRating();
            newRating.setNews(news);
            newRating.setUser(user);
            newRating.setDelta(delta);
            newsRatingRepo.save(newRating);
            news.setRating(news.getRating() + delta);
            userRating = delta;
        }

        newsRepo.save(news);

        Map<String, Object> response = new HashMap<>();
        response.put("rating", news.getRating());
        response.put("userRating", userRating);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/news/user-rating")
    @ResponseBody
    public ResponseEntity<Integer> getUserRating(@RequestParam(required = false) Long newsId, Principal principal) {
        if (newsId == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String username = principal.getName();
        User user = userRepo.findByUserName(username);
        News news = newsRepo.findById(newsId).orElseThrow(() -> new IllegalArgumentException("Invalid news ID: " + newsId));

        Optional<NewsRating> rating = newsRatingRepo.findByNewsAndUser(news, user);
        return ResponseEntity.ok(rating.map(NewsRating::getDelta).orElse(0));
    }
    @GetMapping("/news/rating-info/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRatingInfo(@PathVariable Long id) {
        News news = newsRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid news ID: " + id));
        long positiveCount = newsRatingRepo.countByNewsAndDelta(news, 1);
        long negativeCount = newsRatingRepo.countByNewsAndDelta(news, -1);

        Map<String, Object> response = new HashMap<>();
        response.put("positive", positiveCount);
        response.put("negative", negativeCount);
        response.put("total", news.getRating());

        return ResponseEntity.ok(response);
    }
}
