package com.example.MangaLibrary.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("main/error");
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                modelAndView.addObject("errorMessage", "Сторінка не знайдена");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                modelAndView.addObject("errorMessage", "Внутрішня помилка сервера");
            } else if(statusCode == HttpStatus.FORBIDDEN.value()){
                modelAndView.addObject("errorMessage", "Недостатньо прав для доступу до цієї сторінки");
            }else {
                modelAndView.addObject("errorMessage", "Невідома помилка");
            }
        }
        return modelAndView;
    }
}
