package csd230.lab1.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionAdvice {
    @ExceptionHandler({BookNotFoundException.class, MagazineNotFoundException.class,
            DiscMagNotFoundException.class, TicketNotFoundException.class,
            CartNotFoundException.class, OrderNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> notFoundHandler(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }
}
