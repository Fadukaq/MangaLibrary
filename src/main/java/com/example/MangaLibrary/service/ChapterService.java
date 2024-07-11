package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.manga.ChapterForm;
import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.repo.ChapterRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepo chapterRepo;

    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private MangaLibraryManager mangaLibraryManager;

    public boolean isValidChapterForm(ChapterForm chapterForm, BindingResult bindingResult) {
        boolean isValid = true;

        if (chapterForm.getChapter().getTitle() == null || chapterForm.getChapter().getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("chapter.title", "error.title", "Введіть заголовок глави");
            isValid = false;
        }

        boolean hasNewImages = chapterForm.getChapterImage().getPagesImage() != null &&
                !chapterForm.getChapterImage().getPagesImage().isEmpty() &&
                chapterForm.getChapterImage().getPagesImage().stream().anyMatch(file -> file.getSize() > 0);

        boolean hasExistingImages = chapterForm.getChapterImage().getExistingImages() != null &&
                !chapterForm.getChapterImage().getExistingImages().isEmpty();

        if (!hasNewImages && !hasExistingImages) {
            bindingResult.rejectValue("chapterImage.pagesImage", "error.pagesImage", "Додайте хоча б одне зображення глави або виберіть існуючі");
            isValid = false;
        }

        return isValid;
    }

    public void addChapter(ChapterForm chapterForm, Manga manga) throws IOException {
        Chapter chapter = new Chapter();
        chapter.setTitle(chapterForm.getChapter().getTitle());
        chapter.setManga(manga);
        chapter.setCreationTime(LocalDateTime.now());

        List<String> imageUrls = createPagesManga(chapterForm.getChapterImage().getPagesImage(), manga, chapter);

        chapter.setChapterPages(String.join(",", imageUrls));

        chapterRepo.save(chapter);
    }

    public void editChapter(ChapterForm chapterForm, Manga manga, Chapter chapter) throws IOException {
        chapter.setTitle(chapterForm.getChapter().getTitle());
        chapter.setCreationTime(LocalDateTime.now());

        List<String> imageUrls = new ArrayList<>();
        if (chapterForm.getChapterImage().getPagesImage() != null && chapterForm.getChapterImage().getPagesImage().stream().anyMatch(file -> file.getSize() > 0)) {
            imageUrls = createPagesManga(chapterForm.getChapterImage().getPagesImage(), manga, chapter);
        } else {
            imageUrls = Arrays.asList(chapter.getChapterPages().split(","));
        }

        chapter.setChapterPages(String.join(",", imageUrls));
        chapterRepo.save(chapter);
    }

    public void deleteChapter(Long mangaId, Long chapterId) throws IOException {
        Optional<Manga> mangaOptional = mangaRepo.findById(mangaId);
        Optional<Chapter> chapterOptional = chapterRepo.findById(chapterId);

        if (mangaOptional.isPresent() && chapterOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = chapterOptional.get();

            String cleanMangaName = manga.getMangaName()
                    .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String targetRootPath = mangaLibraryManager.getTargetPathManga();
            String chapterFolderPath = targetRootPath + File.separator + cleanMangaName + File.separator + "chapters" + File.separator + chapter.getTitle();
            File chapterFolder = new File(chapterFolderPath);

            String resourceRootPath = "src/main/resources/static/images/mangas";
            String resourceChapterFolderPath = resourceRootPath + File.separator + cleanMangaName + File.separator + "chapters" + File.separator + chapter.getTitle();
            File resourceChapterFolder = new File(resourceChapterFolderPath);

            if (chapterFolder.exists()) {
                FileUtils.deleteDirectory(chapterFolder);
            }

            if (resourceChapterFolder.exists()) {
                FileUtils.deleteDirectory(resourceChapterFolder);
            }

            chapterRepo.deleteById(chapterId);
        } else {
            throw new IllegalArgumentException("Такої манги або глави не знайдено");
        }
    }

    public List<String> createPagesManga(List<MultipartFile> files, Manga manga, Chapter chapter) throws IOException {
        List<String> pagePaths = new ArrayList<>();
        String cleanMangaName = manga.getMangaName()
                .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        String chapterFolderPath = targetRootPath + File.separator + cleanMangaName + File.separator + "chapters" + File.separator + chapter.getTitle();

        File chapterFolder = new File(chapterFolderPath);
        if (!chapterFolder.exists()) {
            chapterFolder.mkdirs();
        }

        String resourcesPath = "src/main/resources/static/images/mangas/" + cleanMangaName + "/chapters/" + chapter.getTitle();
        Path resourcesFolderPath = Paths.get(resourcesPath);
        if (!Files.exists(resourcesFolderPath)) {
            Files.createDirectories(resourcesFolderPath);
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String originalFilename = file.getOriginalFilename();
            String cleanFilename = originalFilename.replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
            String fileName = cleanMangaName + "_Page" + (i + 1) + "_" + System.currentTimeMillis() + ".png";

            File targetFile = new File(chapterFolder, fileName);
            if (targetFile.exists()) {
                throw new IOException("Файл вже існує: " + targetFile.getAbsolutePath());
            }
            file.transferTo(targetFile);

            Path targetFilePath = Paths.get(resourcesPath, fileName);
            Files.copy(targetFile.toPath(), targetFilePath);

            pagePaths.add(fileName);
        }

        return pagePaths;
    }

    public String cleanStringForUrl(String input) {
        return input.replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
    }
}
