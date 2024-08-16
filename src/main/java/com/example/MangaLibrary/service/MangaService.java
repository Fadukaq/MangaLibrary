package com.example.MangaLibrary.service;

import ch.qos.logback.core.model.Model;
import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.models.Genre;
import com.example.MangaLibrary.models.Manga;
import com.example.MangaLibrary.models.Rating;
import com.example.MangaLibrary.models.User;
import com.example.MangaLibrary.repo.GenreRepo;
import com.example.MangaLibrary.repo.MangaRepo;
import com.example.MangaLibrary.repo.RatingRepo;
import com.example.MangaLibrary.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MangaService {
    @Autowired
    private MangaLibraryManager mangaLibraryManager;
    @Autowired
    private MangaRepo mangaRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private GenreRepo genreRepo;

    @Autowired
    private RatingRepo ratingRepo;
    private static final Map<String, String> statusTranslation = new HashMap<>();

    public boolean isValidAddMangaForm(MangaForm mangaForm, BindingResult bindingResult) {
        List<Long> genreIds = mangaForm.getGenres().stream()
                .map(Genre::getId)
                .toList();

        MultipartFile posterImage = mangaForm.getMangaImage().getPosterImage();
        long fileSizePosterInBytes = posterImage.getSize();
        double fileSizePosterInMB = (double) fileSizePosterInBytes / (1024 * 1024);

        String mangaStatus = mangaForm.getManga().getMangaStatus();
        if (mangaStatus == null || mangaStatus.isEmpty() || !isValidMangaStatus(mangaStatus)) {
            bindingResult.rejectValue("manga.mangaStatus", "error.mangaStatus", "Оберіть коректний статус манги (release, ongoing, completed).");
            return false;
        }
        if (bindingResult.hasErrors()
                || genreIds.isEmpty()
                || mangaForm.getMangaImage().getPosterImage().isEmpty()
                || fileSizePosterInMB > 5) {

            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }

            if (mangaForm.getMangaImage().getPosterImage().isEmpty()) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.missingFile", "Постер манги не був загруженний.");
            }
            if (fileSizePosterInMB > 5) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.fileSize", "Розмір завантажуваної картинки перевищує 5 МБ");
            }
            if(mangaForm.getManga().getAuthor() == null)
            {
                bindingResult.rejectValue("manga.author", "error.author", "Виберіть автора манги.");
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

        if (mangaForm.getManga().getReleaseYear().isEmpty()) {
            bindingResult.rejectValue("manga.releaseYear", "error.releaseYear", "Оберіть рік випуску манги.");
            isValid = false;
        }

        if (genreIds.isEmpty()) {
            bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            isValid = false;
        }
        String mangaStatus = mangaForm.getManga().getMangaStatus();
        if (mangaStatus == null || mangaStatus.isEmpty() || !isValidMangaStatus(mangaStatus)) {
            bindingResult.rejectValue("manga.mangaStatus", "error.mangaStatus", "Оберіть коректний статус манги (release, ongoing, completed).");
            isValid = false;
        }
        if(mangaForm.getManga().getAuthor() == null)
        {
            bindingResult.rejectValue("manga.author", "error.author", "Виберіть автора манги.");
            isValid = false;
        }
        return isValid;
    }
    private boolean isValidMangaStatus(String status) {
        return status.equals("release") || status.equals("ongoing") || status.equals("completed");
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
        mangaRepo.save(mangaForm.getManga());
    }
    public void updateManga(long id, MangaForm mangaForm) {

        Manga mangaToUpdate = mangaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id:" + id));
        mangaToUpdate.setMangaDescription(mangaForm.getManga().getMangaDescription());
        mangaToUpdate.setReleaseYear(mangaForm.getManga().getReleaseYear());
        mangaToUpdate.setGenres(mangaForm.getManga().getGenres());
        mangaToUpdate.setAuthor(mangaForm.getManga().getAuthor());
        mangaToUpdate.setAdultContent(mangaForm.getManga().getAdultContent());
        mangaToUpdate.setMangaStatus(mangaForm.getManga().getMangaStatus());
        if (mangaForm.getMangaImage().getBackGroundMangaImg() != null && !mangaForm.getMangaImage().getBackGroundMangaImg().isEmpty()) {
            String mangaFolderPath = mangaLibraryManager.getResourcePathOfThisManga(mangaToUpdate.getMangaName());
            String StringBackGroundImg = createBackGroundManga(mangaForm.getMangaImage().getBackGroundMangaImg(), mangaToUpdate,mangaFolderPath);
            mangaToUpdate.setMangaBackGround(StringBackGroundImg);
        }
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

    public String getMangaTranslatedStatus(String status)
    {
        statusTranslation.put("release", "Реліз");
        statusTranslation.put("ongoing", "Онгоїнг");
        statusTranslation.put("completed", "Завершено");
        return statusTranslation.getOrDefault(status, status);
    }
    public static void addMangaStatusAttributes(User user, Long mangaId, ModelMap model) {
        List<List<String>> userLists = List.of(
                user.getMangaReading(),
                user.getMangaWantToRead(),
                user.getMangaRecited(),
                user.getMangaStoppedReading()
        );

        String[] attributeNames = {
                "isInReadingList",
                "isInWantToReadList",
                "isInRecitedList",
                "isInReadStoppedList"
        };

        String mangaIdStr = String.valueOf(mangaId);
        for (int i = 0; i < userLists.size(); i++) {
            boolean isInList = userLists.get(i).contains(mangaIdStr);
            model.addAttribute(attributeNames[i], isInList);
        }
    }


    @Transactional
    public void saveRating(Long mangaId, Long userId, int ratingValue) {
        Manga manga = mangaRepo.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Manga not found"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rating rating = ratingRepo.findByMangaAndUser(manga, user)
                .orElse(new Rating());
        rating.setManga(manga);
        rating.setUser(user);
        rating.setRating(ratingValue);

        ratingRepo.save(rating);

        updateAverageRating(manga);
    }

    private void updateAverageRating(Manga manga) {
        double averageRating = ratingRepo.findAverageRatingByManga(manga);
        int totalRatings = ratingRepo.countByManga(manga);

        manga.setAverageRating(averageRating);
        manga.setTotalRatings(totalRatings);
        mangaRepo.save(manga);
    }
    @Transactional
    public void removeRating(Long mangaId, Long userId) {
        Optional<Manga> mangaOptional= mangaRepo.findById(mangaId);
        if(mangaOptional.isPresent())
        {
            Manga manga = mangaOptional.get();
            ratingRepo.deleteByMangaIdAndUserId(mangaId, userId);
            int newTotalRatings = ratingRepo.countByMangaId(mangaId);
            manga.setTotalRatings(newTotalRatings);

            double newAverageRating = (newTotalRatings > 0)
                    ? ratingRepo.findAverageByMangaId(mangaId)
                    : 0.0;
            manga.setAverageRating(newAverageRating);
            mangaRepo.save(manga);
        }
    }
    public void addRelatedManga(Long mangaId, Long relatedMangaId) {
        Manga manga = mangaRepo.findById(mangaId).orElseThrow();
        Manga relatedManga = mangaRepo.findById(relatedMangaId).orElseThrow();

        manga.getRelatedMangas().add(relatedManga);
        mangaRepo.save(manga);
    }

    public Set<Manga> getRelatedMangas(Long mangaId) {
        Manga manga = mangaRepo.findById(mangaId).orElseThrow();
        Set<Manga> relatedMangas = manga.getRelatedMangas();

        List<Manga> reverseRelatedMangas = mangaRepo.findByRelatedMangasId(mangaId);
        relatedMangas.addAll(reverseRelatedMangas);

        return relatedMangas;
    }
    public long getCountByReading(String mangaId) {
        return userRepo.countByMangaReading(mangaId);
    }

    public long getCountByWantToRead(String mangaId) {
        return userRepo.countByMangaWantToRead(mangaId);
    }

    public long getCountByStoppedReading(String mangaId) {
        return userRepo.countByMangaStoppedReading(mangaId);
    }

    public long getCountByRecited(String mangaId) {
        return userRepo.countByMangaRecited(mangaId);
    }

    public long getCountByFavorites(String mangaId) {
        return userRepo.countByMangaFavorites(mangaId);
    }


    public long getCountByOneStar(String mangaId) {
        return userRepo.countByMangaOneStar(Long.parseLong(mangaId));
    }

    public long getCountByTwoStar(String mangaId) {
        return userRepo.countByMangaTwoStar(Long.parseLong(mangaId));
    }

    public long getCountByThreeStar(String mangaId) {
        return userRepo.countByMangaThreeStar(Long.parseLong(mangaId));
    }

    public long getCountByFourStar(String mangaId) {
        return userRepo.countByMangaFourStar(Long.parseLong(mangaId));
    }

    public long getCountByFiveStar(String mangaId) {
        return userRepo.countByMangaFiveStar(Long.parseLong(mangaId));
    }
}

