package com.example.MangaLibrary.service;

import org.springframework.stereotype.Service;

@Service
public class NewsService {
    public String formatNewsText(String text) {
        return text.replaceAll("\n", "<br>");
    }
}
