package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.manga.ChapterForm;
import com.example.MangaLibrary.models.Chapter;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.ChapterRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public void addChapter(ChapterForm chapterForm, Manga manga, User user) throws IOException {
        Chapter chapter = new Chapter();
        chapter.setTitle(chapterForm.getChapter().getTitle());
        chapter.setManga(manga);
        chapter.setUser(user);
        chapter.setCreationTime(LocalDateTime.now());
        chapterRepo.save(chapter);

        List<String> imageUrls = createPagesManga(chapterForm.getChapterImage().getPagesImage(), manga, chapter);

        chapter.setChapterPages(String.join(",", imageUrls));
        chapterRepo.save(chapter);
    }

    public void editChapter(ChapterForm chapterForm, Manga manga, Chapter chapter) throws IOException {
        chapter.setTitle(chapterForm.getChapter().getTitle());
        chapter.setCreationTime(LocalDateTime.now());

        List<String> imageUrls = new ArrayList<>();
        if (chapterForm.getChapterImage().getPagesImage() != null && chapterForm.getChapterImage().getPagesImage().stream().anyMatch(file -> file.getSize() > 0)) {
            clearChapterFolder(manga,chapter);
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
            String chapterFolderPath = targetRootPath + File.separator + manga.getId() + File.separator + "chapters" + File.separator + chapter.getId();
            File chapterFolder = new File(chapterFolderPath);

            String resourceRootPath = "src/main/resources/static/images/mangas";
            String resourceChapterFolderPath = resourceRootPath + File.separator + manga.getId() + File.separator + "chapters" + File.separator + chapter.getId();
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
        Long mangaId  = manga.getId();

        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        String chapterFolderPath = targetRootPath + File.separator + mangaId + File.separator + "chapters" + File.separator + chapter.getId();

        File chapterFolder = new File(chapterFolderPath);
        if (!chapterFolder.exists()) {
            chapterFolder.mkdirs();
        }

        String resourcesPath = "src/main/resources/static/images/mangas/" + mangaId + "/chapters/" + chapter.getId();
        Path resourcesFolderPath = Paths.get(resourcesPath);
        if (!Files.exists(resourcesFolderPath)) {
            Files.createDirectories(resourcesFolderPath);
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String fileName = mangaId + "_Page" + (i + 1) + "_" + System.currentTimeMillis() + ".png";

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

    public void addChapterDataToModel(Model model, Manga manga, Chapter chapter, ChapterForm chapterForm) {
        String[] chapterImageFileNames = chapter.getChapterPages().split(",");
        String[] chapterImageUrls = new String[chapterImageFileNames.length];
        for (int i = 0; i < chapterImageFileNames.length; i++) {
            chapterImageUrls[i] = String.format("/images/mangas/%s/chapters/%s/%s", manga.getId(), chapter.getId(), chapterImageFileNames[i]);
        }

        model.addAttribute("manga", manga);
        model.addAttribute("chapter", chapter);
        model.addAttribute("chapterImageUrls", chapterImageUrls);
        model.addAttribute("chapterForm", chapterForm);
    }
    private void clearChapterFolder(Manga manga, Chapter chapter) {
        String chapterFolderPath = getChapterFolderPath(manga.getId(), chapter.getId());
        File chapterFolder = new File(chapterFolderPath);

        if (chapterFolder.exists() && chapterFolder.isDirectory()) {
            for (File file : chapterFolder.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }
    private String getChapterFolderPath(Long mangaId, Long chapterId) {
        return mangaLibraryManager.getTargetPathManga() + File.separator + mangaId + File.separator + "chapters" + File.separator + chapterId;
    }
}
