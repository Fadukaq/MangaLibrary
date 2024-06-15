package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MangaService {
    @Autowired
    private MangaLibraryManager mangaLibraryManager;
    @Autowired
    private GenreRepo genreRepo;
    @Autowired
    private MangaRepo mangaRepo;
    public boolean isValidAddMangaForm(MangaForm mangaForm, BindingResult bindingResult) {
        List<Long> genreIds = mangaForm.getGenres().stream()
                .map(Genre::getId)
                .toList();

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
                || genreIds.isEmpty()
                || mangaForm.getMangaImage().getPosterImage().isEmpty()
                || mangaForm.getMangaImage().getPagesImage().isEmpty()
                || fileSizePosterInMB > 5
                || fileSizePagesInMB > 10) {

            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }

            if (mangaForm.getMangaImage().getPosterImage().isEmpty()) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.missingFile", "Постер манги не був загруженний.");
            }
            if (fileSizePosterInMB > 5) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.fileSize", "Розмір завантажуваної картинки перевищує 5 МБ");
            }
            if (fileSizePagesInMB > 10) {
                bindingResult.rejectValue("mangaImage.pagesImage", "error.fileSize", "Розмір завантажуванних картинок перевищує 10 МБ");
            }
            if (mangaForm.getMangaImage().getPagesImage() == null || mangaForm.getMangaImage().getPagesImage().stream().anyMatch(file -> file.getSize() == 0)) {
                bindingResult.rejectValue("mangaImage.pagesImage", "error.missingFile", "Сторінки манги не були загружені.");
            }

            return false;
        }

        return true;
    }
    public boolean isValidUpdateMangaForm(MangaForm mangaForm, BindingResult bindingResult) {
        List<Long> genreIds = mangaForm.getManga().getGenres().stream()
                .map(Genre::getId)
                .toList();
        boolean isValid = true;

        if (mangaForm.getManga().getMangaDescription().isEmpty() ||
                mangaForm.getManga().getMangaDescription().length() < 10 || mangaForm.getManga().getMangaDescription().length() > 2048) {
            bindingResult.rejectValue("manga.mangaDescription", "error.mangaDescription", "Опис манги повинен містити від 10 до 2048 символів.");
            isValid = false;
        }

        if (mangaForm.getManga().getMangaAuthor().isEmpty() ||
                mangaForm.getManga().getMangaAuthor().length() < 5 || mangaForm.getManga().getMangaAuthor().length() > 256) {
            bindingResult.rejectValue("manga.mangaAuthor", "error.mangaAuthor", "Ім'я автора манги повинно містити від 5 до 256 символів.");
            isValid = false;
        }

        if (mangaForm.getManga().getReleaseYear().isEmpty()) {
            bindingResult.rejectValue("manga.releaseYear", "error.releaseYear", "Оберіть рік випуску манги.");
            isValid = false;
        }

        if (genreIds.isEmpty()) {
            bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            isValid = false;
        }

        return isValid;
    }
    public void saveManga(MangaForm mangaForm) throws IOException {
        Manga existingManga = mangaRepo.findByMangaName(mangaForm.getManga().getMangaName());
        if (existingManga != null) {
            throw new IllegalArgumentException("Манга з такою назвою вже існує");
        }

        List<Long> genreIds = mangaForm.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());
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
    }
    public void updateManga(long id, MangaForm mangaForm) {

        Manga mangaToUpdate = mangaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id:" + id));
        mangaToUpdate.setMangaDescription(mangaForm.getManga().getMangaDescription());
        mangaToUpdate.setReleaseYear(mangaForm.getManga().getReleaseYear());
        mangaToUpdate.setMangaAuthor(mangaForm.getManga().getMangaAuthor());
        mangaToUpdate.setGenres(mangaForm.getManga().getGenres());

        mangaRepo.save(mangaToUpdate);
    }
    public void deleteFolder(String mangaName)
    {
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
    }

}
