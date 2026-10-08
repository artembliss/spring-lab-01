package kz.iitu.springlab.web;

import java.net.URI;
import java.util.List;

import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookRestController {
    private final BookService service;

    public BookRestController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> list(
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "10") int limit) {
        return service.findAll(author).stream().limit(Math.max(0, limit)).toList();
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam(defaultValue = "") String title) {
        return service.searchByTitle(title);
    }

    @GetMapping("/{id}")
    public Book find(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = service.create(book);
        return ResponseEntity.created(URI.create("/api/books/" + saved.id())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> replace(@PathVariable long id, @RequestBody Book book) {
        return service.replace(id, book)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
