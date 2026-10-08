package kz.iitu.springlab.catalog;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return repository.findAll();
        }
        String fragment = author.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(book -> book.author().toLowerCase(Locale.ROOT).contains(fragment))
                .toList();
    }

    public List<Book> searchByTitle(String title) {
        String fragment = title == null ? "" : title.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(book -> book.title().toLowerCase(Locale.ROOT).contains(fragment))
                .toList();
    }

    public Book findById(long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        return repository.save(new Book(null, book.title(), book.author(), book.year()));
    }

    public Optional<Book> replace(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(repository.save(new Book(id, book.title(), book.author(), book.year())));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}
