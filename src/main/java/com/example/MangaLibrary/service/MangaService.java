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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

        String rootPath = mangaLibraryManager.getResourcePathManga();
        String mangaFolderPath = createFolderForManga(mangaForm.getManga(), rootPath);
        String posterPath = createPosterManga(mangaForm.getMangaImage().getPosterImage(), mangaForm.getManga(), mangaFolderPath);
        mangaForm.getManga().setMangaPosterImg(posterPath);
        if(!mangaForm.getMangaImage().getBackGroundMangaImg().isEmpty()){
            String backGroundPath = createBackGroundManga(mangaForm.getMangaImage().getBackGroundMangaImg(), mangaForm.getManga(), mangaFolderPath);
            mangaForm.getManga().setMangaBackGround(backGroundPath);
        }
        else{
            mangaForm.getManga().setMangaBackGround("/images/mangas/defaultBackGroundManga.jpg");
        }
        List<String> pagesImages = createPagesManga(mangaForm.getMangaImage().getPagesImage(), mangaForm.getManga(), mangaFolderPath);
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
        String rootPath = mangaLibraryManager.getResourcePathManga();
        File sourceFolder = new File(rootPath + File.separator + mangaName);
        if (sourceFolder.exists()) {
            deleteThisFolder(sourceFolder);
        }

        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        File targetFolder = new File(targetRootPath + File.separator + mangaName);
        if (targetFolder.exists()) {
            deleteThisFolder(targetFolder);
        }
    }

    public String createFolderForManga(Manga thisManga, String rootPath) {
        String mangaFolderName = thisManga.getMangaName().replaceAll("\\s", "_")
                .replaceAll("[^\\p{L}\\p{N}.\\-_]", "");
        File mangaFolder = new File(rootPath + File.separator + mangaFolderName);

        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        File mangaFolderTarget = new File(targetRootPath + File.separator + mangaFolderName);

        if (!mangaFolder.exists() || !mangaFolderTarget.exists()) {
            mangaFolder.mkdirs();
            mangaFolderTarget.mkdirs();
        }

        return mangaFolder.getAbsolutePath();
    }

    public List<String> createPagesManga(List<MultipartFile> pagesManga, Manga thisManga, String mangaFolderPath) throws IOException {
        List<String> pagePaths = new ArrayList<>();
        String cleanMangaName = thisManga.getMangaName()
                .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

        File sourceFolder = new File(mangaFolderPath);
        if (!sourceFolder.exists()) {
            sourceFolder.mkdirs();
        }
        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        File targetFolder = new File(targetRootPath + File.separator + cleanMangaName);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        for (int i = 0; i < pagesManga.size(); i++) {
            String fileName = cleanMangaName + "_Page" + (i + 1) + ".png";

            File sourceFile = new File(sourceFolder + File.separator + fileName);
            pagesManga.get(i).transferTo(sourceFile);

            File targetFile = new File(targetFolder + File.separator + fileName);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            pagePaths.add(fileName);
        }
        return pagePaths;
    }

    public String createPosterManga(MultipartFile posterImg,Manga thisManga,String mangaFolderPath) {
        try {
            byte[] bytes = posterImg.getBytes();
            String cleanMangaName = thisManga.getMangaName()
                    .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String fileName = cleanMangaName + "_Poster.png";

            File targetFile = new File(mangaFolderPath + "/" + fileName);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            outputStream.write(bytes);
            outputStream.close();

            String targetRootPath = mangaLibraryManager.getTargetPathManga();
            File targetFolder = new File(targetRootPath + File.separator + cleanMangaName);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File sourseFile = new File(targetFolder + File.separator + fileName);
            FileOutputStream targetOutputStream = new FileOutputStream(sourseFile);
            targetOutputStream.write(bytes);
            targetOutputStream.close();

            return "/images/mangas/" + cleanMangaName + File.separator + fileName;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "redirect:/manga/add";
        }
    }
    public String createBackGroundManga(MultipartFile backgroundImg, Manga thisManga,String mangaFolderPath) {
        try {
            byte[] bytes = backgroundImg.getBytes();
            String cleanMangaName = thisManga.getMangaName()
                    .replaceAll("\\s", "_").replaceAll("[^\\p{L}\\p{N}.\\-_]", "");

            String fileName = cleanMangaName + "_BackGround.png";

            File targetFile = new File(mangaFolderPath + "/" + fileName);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            outputStream.write(bytes);
            outputStream.close();

            String targetRootPath = mangaLibraryManager.getTargetPathManga();
            File targetFolder = new File(targetRootPath + File.separator + cleanMangaName);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File sourseFile = new File(targetFolder + File.separator + fileName);
            FileOutputStream targetOutputStream = new FileOutputStream(sourseFile);
            targetOutputStream.write(bytes);
            targetOutputStream.close();

            return "/images/mangas/" + cleanMangaName + "/" + fileName;
        }
        catch (IOException e) {
        e.printStackTrace();
        return "redirect:/manga/add";
        }
    }

    public Long getRandomMangaId() {
        Iterable<Manga> mangas = mangaRepo.findAll();
        List<Manga> mangaList = StreamSupport.stream(mangas.spliterator(), false)
                .collect(Collectors.toList());
        if (mangaList.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(mangaList.size());
        Manga randomManga = mangaList.get(randomIndex);
        return randomManga.getId();
    }

    public void deleteThisFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteThisFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

}

