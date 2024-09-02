package com.example.MangaLibrary.models;

public class MangaDTO {
    private long id;
    private String mangaName;
    private String mangaDescription;
    private String mangaStatus;
    private String mangaPosterImg;
    private String mangaBackGround;
    private String releaseYear;
    private Boolean adultContent;
    private double averageRating;
    private int totalRatings;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMangaName() {
        return mangaName;
    }

    public void setMangaName(String mangaName) {
        this.mangaName = mangaName;
    }

    public String getMangaDescription() {
        return mangaDescription;
    }

    public void setMangaDescription(String mangaDescription) {
        this.mangaDescription = mangaDescription;
    }

    public String getMangaStatus() {
        return mangaStatus;
    }

    public void setMangaStatus(String mangaStatus) {
        this.mangaStatus = mangaStatus;
    }

    public String getMangaPosterImg() {
        return mangaPosterImg;
    }

    public void setMangaPosterImg(String mangaPosterImg) {
        this.mangaPosterImg = mangaPosterImg;
    }

    public String getMangaBackGround() {
        return mangaBackGround;
    }

    public void setMangaBackGround(String mangaBackGround) {
        this.mangaBackGround = mangaBackGround;
    }

    public String getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Boolean getAdultContent() {
        return adultContent;
    }

    public void setAdultContent(Boolean adultContent) {
        this.adultContent = adultContent;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }
}
