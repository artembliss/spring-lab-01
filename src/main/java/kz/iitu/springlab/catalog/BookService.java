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

    public Optional<Book> findOptionalById(long id) {
        return repository.findById(id);
    }

    public Book findById(long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        if (repository.existsByTitleIgnoreCase(book.title())) {
            throw new DuplicateTitleException(book.title());
        }
        return repository.save(book);
    }

    public Book update(long id, Book book) {
        Book existing = findById(id);
        Book updated = new Book(
                id,
                book.title(),
                book.author(),
                book.year(),
                book.genre() != null ? book.genre() : existing.genre(),
                existing.createdAt()
        );
        return repository.save(updated);
    }

    public Optional<Book> replace(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(update(id, book));
    }

    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new BookNotFoundException(id);
        }
    }
}
