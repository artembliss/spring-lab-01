package kz.iitu.springlab.catalog;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import kz.iitu.springlab.web.Genre;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch", 2018, Genre.TECHNOLOGY, Instant.now()));
        save(new Book(null, "Clean Code", "Robert Martin", 2008, Genre.TECHNOLOGY, Instant.now()));
        save(new Book(null, "Spring in Action", "Craig Walls", 2022, Genre.TECHNOLOGY, Instant.now()));
    }

    public List<Book> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Book::id))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public boolean existsByTitleIgnoreCase(String title) {
        if (title == null) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(b -> b.title().equalsIgnoreCase(title.trim()));
    }

    public Book save(Book book) {
        long id = book.id() != null ? book.id() : sequence.incrementAndGet();
        sequence.accumulateAndGet(id, Math::max);
        Instant createdAt = book.createdAt() != null ? book.createdAt() : Instant.now();
        Genre genre = book.genre() != null ? book.genre() : Genre.FICTION;
        Book stored = new Book(id, book.title(), book.author(), book.year(), genre, createdAt);
        storage.put(id, stored);
        return stored;
    }

    public boolean deleteById(long id) {
        return storage.remove(id) != null;
    }
}
