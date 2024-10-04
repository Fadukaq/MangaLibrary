package com.example.MangaLibrary.service;

import com.example.MangaLibrary.helper.MangaLibraryManager;
import com.example.MangaLibrary.helper.manga.MangaForm;
import com.example.MangaLibrary.models.*;
import com.example.MangaLibrary.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Year;
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
    private AuthorRepo authorRepo;
    @Autowired
    private RatingRepo ratingRepo;
    @Autowired
    private NotificationService notificationService;
    private static final Map<String, String> statusTranslation = new HashMap<>();

    static {
        statusTranslation.put("release", "Реліз");
        statusTranslation.put("ongoing", "Онгоїнг");
        statusTranslation.put("completed", "Завершено");
    }

    public boolean isValidAddMangaForm(MangaForm mangaForm, BindingResult bindingResult) {
        List<Genre> genres = mangaForm.getGenres();
        List<Long> genreIds = (genres != null) ? genres.stream()
                .map(Genre::getId)
                .toList()
                : Collections.emptyList();

        MultipartFile posterImage = mangaForm.getMangaImage().getPosterImage();
        long fileSizePosterInBytes = posterImage != null ? posterImage.getSize() : 0;
        double fileSizePosterInMB = (double) fileSizePosterInBytes / (1024 * 1024);

        String mangaStatus = mangaForm.getManga().getMangaStatus();
        if (mangaStatus == null || mangaStatus.isEmpty() || !isValidMangaStatus(mangaStatus)) {
            bindingResult.rejectValue("manga.mangaStatus", "error.mangaStatus", "Оберіть коректний статус манги (release, ongoing, completed).");
            return false;
        }

        if (bindingResult.hasErrors()
                || genreIds.isEmpty()
                || (posterImage != null && posterImage.isEmpty())
                || fileSizePosterInMB > 5) {

            if (genreIds.isEmpty()) {
                bindingResult.rejectValue("genres", "error.genres", "Будь ласка, оберіть хоча б один жанр.");
            }

            if (posterImage == null || posterImage.isEmpty()) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.missingFile", "Постер манги не був загруженний.");
            }

            if (fileSizePosterInMB > 5) {
                bindingResult.rejectValue("mangaImage.posterImage", "error.fileSize", "Розмір завантажуваної картинки перевищує 5 МБ");
            }

            if (mangaForm.getManga().getAuthor() == null) {
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
    public void saveManga(MangaForm mangaForm, List<Long> relatedMangaIds, User user) throws IOException {
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

        Manga manga = mangaForm.getManga();
        mangaRepo.save(manga);
        if (relatedMangaIds != null && !relatedMangaIds.isEmpty()) {
            setRelatedMangas(manga.getId(), relatedMangaIds);
        }
        String rootPath = mangaLibraryManager.getResourcePathManga();
        String mangaFolderPath = createFolderForManga(manga.getId(), rootPath);

        String posterPath = createPosterManga(mangaForm.getMangaImage().getPosterImage(), manga, mangaFolderPath);
        manga.setMangaPosterImg(posterPath);
        manga.setPublishedBy(user);

        if (mangaForm.getMangaImage().getBackGroundMangaImg() != null && !mangaForm.getMangaImage().getBackGroundMangaImg().isEmpty()) {
            String backGroundPath = createBackGroundManga(mangaForm.getMangaImage().getBackGroundMangaImg(), manga, mangaFolderPath);
            manga.setMangaBackGround(backGroundPath);
        } else {
            manga.setMangaBackGround("/images/mangas/defaultBackGroundManga.jpg");
        }
        mangaRepo.save(manga);
        notificationService.notifyUsersAboutNewManga(manga.getAuthor(),manga);
    }
    public void updateManga(long id, MangaForm mangaForm, List<Long> relatedMangaIds) {
        Manga mangaToUpdate = mangaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid manga Id:" + id));

        mangaToUpdate.setMangaName(mangaForm.getManga().getMangaName());
        mangaToUpdate.setMangaDescription(mangaForm.getManga().getMangaDescription());
        mangaToUpdate.setReleaseYear(mangaForm.getManga().getReleaseYear());
        mangaToUpdate.setGenres(mangaForm.getManga().getGenres());
        mangaToUpdate.setAuthor(mangaForm.getManga().getAuthor());
        mangaToUpdate.setAdultContent(mangaForm.getManga().getAdultContent());
        mangaToUpdate.setMangaStatus(mangaForm.getManga().getMangaStatus());
        updateRelatedMangas(mangaToUpdate, relatedMangaIds);

        String mangaFolderPath = mangaLibraryManager.getResourcePathManga() + File.separator + id;
        createFolderForManga(id, mangaLibraryManager.getResourcePathManga());
        System.out.println(mangaFolderPath);
        if (mangaForm.getMangaImage().getBackGroundMangaImg() != null && !mangaForm.getMangaImage().getBackGroundMangaImg().isEmpty()) {
            String backgroundImgPath = createBackGroundManga(mangaForm.getMangaImage().getBackGroundMangaImg(), mangaToUpdate, mangaFolderPath);
            mangaToUpdate.setMangaBackGround(backgroundImgPath);
        }
        if (mangaForm.getMangaImage().getPosterImage() != null && !mangaForm.getMangaImage().getPosterImage().isEmpty()) {
            String posterImgPath = createPosterManga(mangaForm.getMangaImage().getPosterImage(), mangaToUpdate, mangaFolderPath);
            mangaToUpdate.setMangaPosterImg(posterImgPath);
        }
        mangaRepo.save(mangaToUpdate);
    }
    public void deleteFolder(long mangaId)
    {
        String rootPath = mangaLibraryManager.getResourcePathManga();
        File sourceFolder = new File(rootPath + File.separator + mangaId);
        if (sourceFolder.exists()) {
            deleteThisFolder(sourceFolder);
        }

        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        File targetFolder = new File(targetRootPath + File.separator + mangaId);
        if (targetFolder.exists()) {
            deleteThisFolder(targetFolder);
        }
    }

    public String createFolderForManga(Long mangaFolderName, String rootPath) {

        File mangaFolder = new File(rootPath + File.separator + mangaFolderName);

        String targetRootPath = mangaLibraryManager.getTargetPathManga();
        File mangaFolderTarget = new File(targetRootPath + File.separator + mangaFolderName);

        if (!mangaFolder.exists() || !mangaFolderTarget.exists()) {
            mangaFolder.mkdirs();
            mangaFolderTarget.mkdirs();
        }

        return mangaFolder.getAbsolutePath();
    }

    public String createPosterManga(MultipartFile posterImg, Manga thisManga, String mangaFolderPath) {
        try {
            byte[] bytes = posterImg.getBytes();
            String mangaId = String.valueOf(thisManga.getId());
            String fileName = mangaId + "_Poster.png";

            String targetRootPath = mangaLibraryManager.getTargetPathManga();
            File targetFolder = new File(targetRootPath + File.separator + mangaId);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File sourceFile = new File(targetFolder + File.separator + fileName);
            try (FileOutputStream targetOutputStream = new FileOutputStream(sourceFile)) {
                targetOutputStream.write(bytes);
            }
            System.out.println("Poster saved in target path: " + sourceFile.getAbsolutePath());

            File resourcesFolder = new File(mangaFolderPath);
            if (!resourcesFolder.exists()) {
                resourcesFolder.mkdirs();
            }
            File targetFile = new File(resourcesFolder, fileName);
            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                outputStream.write(bytes);
            }
            System.out.println("Poster saved in resources path: " + targetFile.getAbsolutePath());

            return "/images/mangas/" + mangaId + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "none";
        }
    }


    public String createBackGroundManga(MultipartFile backgroundImg, Manga thisManga, String mangaFolderPath) {
        try {
            byte[] bytes = backgroundImg.getBytes();
            String mangaId = String.valueOf(thisManga.getId());
            String fileName = mangaId + "_BackGround.png";

            String targetRootPath = mangaLibraryManager.getTargetPathManga();
            File targetFolder = new File(targetRootPath + File.separator + mangaId);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            File targetFile = new File(targetFolder, fileName);
            try (FileOutputStream targetOutputStream = new FileOutputStream(targetFile)) {
                targetOutputStream.write(bytes);
            }

            File resourcesFolder = new File(mangaFolderPath + File.separator + mangaId);
            if (!resourcesFolder.exists()) {
                resourcesFolder.mkdirs();
            }
            File resourcesFile = new File(resourcesFolder, fileName);
            try (FileOutputStream resourcesOutputStream = new FileOutputStream(resourcesFile)) {
                resourcesOutputStream.write(bytes);
            }

            return "/images/mangas/" + mangaId + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "none";
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


    public String getMangaTranslatedStatus(String status) {
        return statusTranslation.getOrDefault(status, status);
    }

    public Set<Manga> getRelatedMangas(Long mangaId) {
        Manga manga = mangaRepo.findById(mangaId).orElseThrow();
        Set<Manga> relatedMangas = manga.getRelatedMangas();

        List<Manga> reverseRelatedMangas = mangaRepo.findByRelatedMangasId(mangaId);
        relatedMangas.addAll(reverseRelatedMangas);

        return relatedMangas;
    }
    public void setRelatedMangas(Long mangaId, List<Long> relatedMangaIds) {
        Manga manga = mangaRepo.findById(mangaId)
                .orElseThrow(() -> new RuntimeException("Manga not found: " + mangaId));

        List<Manga> relatedMangas = (List<Manga>) mangaRepo.findAllById(relatedMangaIds);

        manga.getRelatedMangas().addAll(relatedMangas);
        mangaRepo.save(manga);

        for (Manga relatedManga : relatedMangas) {
            relatedManga.getRelatedMangas().add(manga);
            mangaRepo.save(relatedManga);
        }
    }
    private void updateRelatedMangas(Manga manga, List<Long> relatedMangaIds) {
        for (Manga relatedManga : new ArrayList<>(manga.getRelatedMangas())) {
            relatedManga.getRelatedMangas().remove(manga);
            manga.getRelatedMangas().remove(relatedManga);
            mangaRepo.save(relatedManga);
        }

        if (relatedMangaIds != null && !relatedMangaIds.isEmpty()) {
            List<Manga> relatedMangas = (List<Manga>) mangaRepo.findAllById(relatedMangaIds);
            for (Manga relatedManga : relatedMangas) {
                manga.getRelatedMangas().add(relatedManga);
                relatedManga.getRelatedMangas().add(manga);
                mangaRepo.save(relatedManga);
            }
        }
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

