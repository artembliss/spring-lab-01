package kz.iitu.springlab.catalog;

import java.util.List;
import kz.iitu.springlab.web.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    public BookService(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            PublisherRepository publisherRepository
    ) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
    }

    public List<Book> findAll(String author) {
        if (author == null || author.isBlank()) {
            return bookRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
        return bookRepository.findByAuthorNameContainingIgnoreCase(author.trim());
    }

    public Page<Book> find(Genre genre, Pageable pageable) {
        if (genre != null) {
            return bookRepository.findByGenre(genre, pageable);
        }
        return bookRepository.findAll(pageable);
    }

    public List<Book> searchByTitle(String title) {
        String fragment = title == null ? "" : title.trim();
        return bookRepository.findByTitleContainingIgnoreCase(fragment);
    }

    public Book findById(long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book create(Book book) {
        if (bookRepository.existsByTitleIgnoreCase(book.getTitle())) {
            throw new DuplicateTitleException(book.getTitle());
        }

        if (book.getAuthor() != null && book.getAuthor().getName() != null) {
            String authorName = book.getAuthor().getName().trim();
            Author author = authorRepository.findByName(authorName)
                    .orElseGet(() -> authorRepository.save(new Author(authorName, "Unknown")));
            book.setAuthor(author);
        }

        return bookRepository.save(book);
    }

    @Transactional
    public Book update(long id, Book book) {
        Book existing = findById(id);
        existing.setTitle(book.getTitle());
        if (book.getGenre() != null) {
            existing.setGenre(book.getGenre());
        }
        existing.setYear(book.getYear());

        if (book.getAuthor() != null && book.getAuthor().getName() != null) {
            String authorName = book.getAuthor().getName().trim();
            Author author = authorRepository.findByName(authorName)
                    .orElseGet(() -> authorRepository.save(new Author(authorName, "Unknown")));
            existing.setAuthor(author);
        }

        return bookRepository.save(existing);
    }

    @Transactional
    public void delete(long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public int archiveOlderThan(int year) {
        return bookRepository.archiveOlderThan(year);
    }

    public List<Book> findAllWithAuthor() {
        return bookRepository.findAllWithAuthor();
    }

    public List<Book> findWithGraphByGenre(Genre genre) {
        return bookRepository.findWithGraphByGenre(genre);
    }

    public List<Book> findByPublisherName(String name) {
        return bookRepository.findByPublisherName(name);
    }

    public BookRepository getRepository() {
        return bookRepository;
    }
}
