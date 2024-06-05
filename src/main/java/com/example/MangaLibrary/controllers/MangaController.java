package com.example.MangaLibrary.controllers;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private UserRepo userRepo;
    @Autowired
    private MangaLibraryManager mangaLibraryManager;
    private static final int PAGE_SIZE = 6;
    @GetMapping("/manga")
    public String mangas(
            @RequestParam(name = "page", defaultValue = "1", required = false) int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE, Sort.by("id").descending());
        Page<Manga> mangaPage = mangaRepo.findAll(pageable);

        List<Manga> mangaList = mangaPage.getContent();
        int totalPages = mangaPage.getTotalPages();
        if (page > totalPages) {
            return "redirect:/manga?page=" + totalPages;
        }
        model.addAttribute("mangas", mangaList);
        model.addAttribute("page", mangaPage);
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
        model.addAttribute("genres", genres);

        return "manga/manga-add";
    }
    @PostMapping("/manga/add")
    public String addManga(@ModelAttribute("mangaForm") @Valid MangaForm mangaForm, BindingResult bindingResult,Model model) throws IOException {
        List<Long> genreIds = mangaForm.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        MultipartFile posterImage = mangaForm.getMangaImage().getPosterImage();
        long fileSizePosterInBytes = posterImage.getSize();
        double fileSizePosterInMB = (double) fileSizePosterInBytes / (1024 * 1024);

        List<MultipartFile> pagesImg = mangaForm.getMangaImage().getPagesImage();
        long fileSizePagesInBytes = 0;
        if (pagesImg != null) {
            for (MultipartFile pageImage : pagesImg) {
                fileSizePagesInBytes += pageImage.getSize();
            }
        }
        double fileSizePagesInMB = (double) fileSizePagesInBytes / (1024 * 1024);

        if (bindingResult.hasErrors()
                || (genreIds.isEmpty())
                || (mangaForm.getMangaImage().getPosterImage().isEmpty())
                || (mangaForm.getMangaImage().getPagesImage().isEmpty()
                || (fileSizePosterInMB > 5)
                || (fileSizePagesInMB > 10))) {
            int maxYear = Year.now().getValue();
            List<Genre> genres = genreRepo.findAll();
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);

            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }

            if (mangaForm.getMangaImage().getPosterImage().isEmpty()) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.missingFile", "Постер манги не був загруженний.");
            }
            if(fileSizePosterInMB > 5) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.fileSize", "Розмір завантажуваної картинки перевищує 5 МБ");
            }
            if(fileSizePagesInMB > 10) {
                bindingResult.rejectValue("mangaImage.pagesImage", "error.fileSize", "Розмір завантажуванних картинок перевищує 10 МБ");
            }
            if (mangaForm.getMangaImage().getPagesImage() == null || mangaForm.getMangaImage().getPagesImage().stream().anyMatch(file -> file.getSize() == 0)) {
                bindingResult.rejectValue("mangaImage.pagesImage", "error.missingFile", "Сторінки манги не були загружені.");
            }

            return "manga/manga-add";
        }
        Manga existingManga = mangaRepo.findByMangaName(mangaForm.getManga().getMangaName());
        if (existingManga != null) {
            bindingResult.rejectValue("manga.mangaName", "error.manga", "Манга з такою назвою вже існує");
            return "manga/manga-add";
        }
        Iterable<Genre> genreIterable = genreRepo.findAllById(genreIds);
        List<Genre> selectedGenres = StreamSupport.stream(genreIterable.spliterator(), false)
                .collect(Collectors.toList());
        mangaForm.getManga().setGenres(selectedGenres);

        String rootPath = mangaLibraryManager.getResourcePath();
        String mangaFolderPath = mangaLibraryManager.createFolderForManga(mangaForm.getManga(), rootPath);
        String posterPath = mangaLibraryManager.createPosterManga(mangaForm.getMangaImage().getPosterImage(), mangaForm.getManga(), mangaFolderPath);
        mangaForm.getManga().setMangaPosterImg(posterPath);
        List<String> pagesImages = mangaLibraryManager.createPagesManga(mangaForm.getMangaImage().getPagesImage(), mangaForm.getManga(), mangaFolderPath);
        String pagesImagesAsString = String.join(",", pagesImages);
        mangaForm.getManga().setMangaPages(pagesImagesAsString);

        mangaRepo.save(mangaForm.getManga());
        return "redirect:/manga";
    }

    @PostMapping("/manga/page/{mangaId}")
    public String viewMangaPage(@PathVariable Long mangaId, Model model) {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        if (mangaOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            String[] mangaPages = manga.getMangaPages().split(","); // Получаем список страниц манги
            String mangaFolder = manga.getMangaName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String[] mangaPagesWithFullPath = new String[mangaPages.length];

            for (int i = 0; i < mangaPages.length; i++) {
                mangaPagesWithFullPath[i] = "/images/mangas/" + mangaFolder + "/" + mangaPages[i];
                manga.setMangaPages(mangaPagesWithFullPath[i]);
            }

            model.addAttribute("mangaPages", mangaPagesWithFullPath);
            model.addAttribute("id", mangaId); // Передача id в модель

            return "manga/manga-page";
        } else {
            model.addAttribute("errorMessage", "Такой манги не найдено!");
            return "error";
        }
    }
    @GetMapping("/manga/{id}")
    public String mangaDetails(@PathVariable(value = "id") long id, Model model) {
        Optional<Manga> optionalManga = mangaRepo.findById(id);
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            model.addAttribute("manga", manga);
            return "manga/manga-details";
        } else {
            model.addAttribute("errorMessage", "Такой манги не найдено!");
            return "error";
        }
    }

    @PostMapping("/manga/delete/{id}")
    public String MangaPostDelete(@PathVariable(value ="id") long id,Model model) {
        Manga mangaToDelete = mangaRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
        mangaRepo.delete(mangaToDelete);

        String mangaName = mangaToDelete.getMangaName().replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

        String rootPath = mangaLibraryManager.getResourcePath();
        File sourceFolder = new File(rootPath + File.separator + mangaName);
        if (sourceFolder.exists()) {
            mangaLibraryManager.deleteFolder(sourceFolder);
        }

        String targetRootPath = mangaLibraryManager.getTargetPath();
        File targetFolder = new File(targetRootPath + File.separator + mangaName);
        if (targetFolder.exists()) {
            mangaLibraryManager.deleteFolder(targetFolder);
        }

        Iterable<User> usersIterable = userRepo.findAll();
        List<User> users = new ArrayList<>();
        usersIterable.forEach(users::add);

        for (User user : users) {
            if (user.getMangaReading().contains(String.valueOf(id))) {
                user.getMangaReading().remove(String.valueOf(id));
            }
            if (user.getMangaWantToRead().contains(String.valueOf(id))) {
                user.getMangaWantToRead().remove(String.valueOf(id));
            }
            if (user.getMangaStoppedReading().contains(String.valueOf(id))) {
                user.getMangaStoppedReading().remove(String.valueOf(id));
            }
            if (user.getMangaRecited().contains(String.valueOf(id))) {
                user.getMangaRecited().remove(String.valueOf(id));
            }
            userRepo.save(user);
        }
        return "redirect:/manga";
    }
    @GetMapping("/manga/edit/{id}")
    public String mangaEdit(@PathVariable("id") long id, Model model) {

        Optional<Manga> optionalManga = mangaRepo.findById(id);
        int maxYear = Year.now().getValue();
        List<Genre> genres = genreRepo.findAll();
        if (optionalManga.isPresent()) {
            Manga manga = optionalManga.get();
            MangaForm mangaForm = new MangaForm();
            mangaForm.setManga(manga);

            model.addAttribute("mangaDescription",manga.getMangaDescription() );
            model.addAttribute("mangaReleaseYear", manga.getReleaseYear());
            model.addAttribute("mangaAuthor", manga.getMangaAuthor());
            model.addAttribute("oldGenres", manga.getGenres());
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("mangaForm", mangaForm); // Передаем объект MangaForm в модель
            return "manga/manga-edit";
        } else {
            return "redirect:/manga";
        }
    }

    @PostMapping("/manga/edit/{id}")
    public String mangaPostUpdate(@PathVariable("id") long id, @ModelAttribute("mangaForm") @Valid MangaForm mangaForm,
                                    BindingResult bindingResult, Model model) {
        List<Long> genreIds = mangaForm.getManga().getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
        if(    ((mangaForm.getManga().getMangaDescription().isEmpty()) ||
                mangaForm.getManga().getMangaDescription().length() < 10 || mangaForm.getManga().getMangaDescription().length() > 2048)
                ||
                ((mangaForm.getManga().getMangaAuthor().isEmpty())||
                mangaForm.getManga().getMangaAuthor().length() < 5 || mangaForm.getManga().getMangaAuthor().length() > 256)
                ||
                mangaForm.getManga().getReleaseYear().isEmpty()
                ||
                (genreIds.isEmpty())) {

            int maxYear = Year.now().getValue();
            List<Genre> genres = genreRepo.findAll();
            model.addAttribute("id", id);
            model.addAttribute("maxYear", maxYear);
            model.addAttribute("genres", genres);
            model.addAttribute("mangaForm", mangaForm);
            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }
            return "manga/manga-edit";
        }

        Manga mangaToUpdate = mangaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id:" + id));

        mangaToUpdate.setMangaDescription(mangaForm.getManga().getMangaDescription());
        mangaToUpdate.setReleaseYear(mangaForm.getManga().getReleaseYear());
        mangaToUpdate.setMangaAuthor(mangaForm.getManga().getMangaAuthor());
        mangaToUpdate.setGenres(mangaForm.getManga().getGenres());

        mangaRepo.save(mangaToUpdate);
        return "redirect:/manga";
    }
    @GetMapping("/search")
    public String postSearch(@RequestParam("q") String searchQuery,
                             @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                             Model model) {
        if (!searchQuery.isEmpty()) {
            Page<Manga> foundMangas = mangaRepo.findByMangaNameContainingIgnoreCase(searchQuery, PageRequest.of(page - 1,PAGE_SIZE, Sort.by("id").descending()));
            List<Manga> mangaList = foundMangas.getContent();
            if (mangaList.isEmpty()) {
                model.addAttribute("noResults", true);
                model.addAttribute("searchQuery", searchQuery);
            } else {
                model.addAttribute("noResults", false);
                model.addAttribute("mangas", mangaList);
                model.addAttribute("currentPage", foundMangas.getNumber());
                model.addAttribute("totalPages", foundMangas.getTotalPages());
                model.addAttribute("searchQuery", searchQuery);
            }

            return "manga/manga-search";
        }
        return "redirect:/manga";
    }
    @GetMapping("/manga/reading/{id}")
    public String addMangaToReadingList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        mangaLibraryManager.removeMangaFromOtherLists(user, id);

        user.getMangaReading().add(String.valueOf(id));
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("notificationMessage", "Манга добавлена в список 'Читаю'");

        return "redirect:/manga/" + id;
    }
    @GetMapping("/manga/want-read/{id}")
    public String addMangaToWantReadList(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        mangaLibraryManager.removeMangaFromOtherLists(user, id);

        user.getMangaWantToRead().add(String.valueOf(id));
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("notificationMessage", "Манга добавлена в список 'Буду читать'");

        return "redirect:/manga/" + id;
    }

    @GetMapping("/manga/recited/{id}")
    public String addMangaToRecitedList(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        mangaLibraryManager.removeMangaFromOtherLists(user, id);

        user.getMangaRecited().add(String.valueOf(id));
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("notificationMessage", "Манга добавлена в список 'Прочитал'");

        return "redirect:/manga/" + id;    }

    @GetMapping("/manga/read-stopped/{id}")
    public String addMangaToReadStoppedList(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepo.findByUserName(username);

        mangaLibraryManager.removeMangaFromOtherLists(user, id);
        user.getMangaStoppedReading().add(String.valueOf(id));
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("notificationMessage", "Манга добавлена в список 'Бросил'");

        return "redirect:/manga/" + id;
    }

    @GetMapping("/random")
    public String getRandomMangaId(Model model) {

        Long randomMangaId = mangaLibraryManager.getRandomMangaId();

        model.addAttribute("randomMangaId", randomMangaId);
        if(randomMangaId==null) {
            return "redirect:/manga";
        }
        return "redirect:/manga/" + randomMangaId;
    }
}
