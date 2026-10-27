package kz.iitu.springlab.web.dto;

import java.util.List;
import kz.iitu.springlab.catalog.Author;
import kz.iitu.springlab.catalog.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toDomain(BookRequest request) {
        Book book = new Book(request.title(), request.genre(), request.year());
        if (request.author() != null && !request.author().isBlank()) {
            book.setAuthor(new Author(request.author().trim(), "Unknown"));
        }
        return book;
    }

    public BookResponse toResponse(Book book) {
        String authorName = book.getAuthor() != null ? book.getAuthor().getName() : null;
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                authorName,
                book.getYear(),
                book.getGenre(),
                book.getCreatedAt()
        );
    }

    public List<BookResponse> toResponseList(List<Book> books) {
        return books.stream().map(this::toResponse).toList();
    }
}
