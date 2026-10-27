package kz.iitu.springlab.catalog;

import java.time.Instant;
import kz.iitu.springlab.web.Genre;

public record Book(
        Long id,
        String title,
        String author,
        int year,
        Genre genre,
        Instant createdAt
) {
    public Book(Long id, String title, String author, int year) {
        this(id, title, author, year, Genre.FICTION, Instant.now());
    }

    public Book(Long id, String title, String author, int year, Genre genre) {
        this(id, title, author, year, genre != null ? genre : Genre.FICTION, Instant.now());
    }
}
