package kz.iitu.springlab.web;

import kz.iitu.springlab.catalog.BookNotFoundException;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = BookPageController.class)
public class PageExceptionHandler {
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(BookNotFoundException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/not-found";
    }
}
