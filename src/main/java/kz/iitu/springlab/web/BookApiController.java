package kz.iitu.springlab.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import kz.iitu.springlab.web.dto.BookMapper;
import kz.iitu.springlab.web.dto.BookRequest;
import kz.iitu.springlab.web.dto.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
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

@Tag(name = "Books", description = "The catalogue of books")
@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService service;
    private final BookMapper mapper;

    public BookApiController(BookService service, BookMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "List books with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Page of books")
    @GetMapping
    public Page<BookResponse> list(
            @Parameter(description = "Filter by genre")
            @RequestParam(required = false) Genre genre,
            @Parameter(description = "Filter by author name")
            @RequestParam(required = false) String author,
            @PageableDefault(size = 5, sort = "title") Pageable pageable
    ) {
        if (author != null && !author.isBlank()) {
            return service.getRepository()
                    .findByAuthorNameContainingIgnoreCase(author.trim(), pageable)
                    .map(mapper::toResponse);
        }
        return service.find(genre, pageable).map(mapper::toResponse);
    }

    @Operation(summary = "Search books by title")
    @ApiResponse(responseCode = "200", description = "Books matching the search title")
    @GetMapping("/search")
    public List<BookResponse> search(
            @Parameter(description = "Title fragment")
            @RequestParam(defaultValue = "") String title
    ) {
        return mapper.toResponseList(service.searchByTitle(title));
    }

    @Operation(summary = "Find books by publisher name (Variant 1)")
    @ApiResponse(responseCode = "200", description = "Books belonging to the publisher")
    @GetMapping("/publisher")
    public List<BookResponse> byPublisher(@RequestParam String name) {
        return mapper.toResponseList(service.findByPublisherName(name));
    }

    @Operation(summary = "Find top 3 books by genre ordered by year descending")
    @ApiResponse(responseCode = "200", description = "Top 3 books in genre")
    @GetMapping("/top3")
    public List<BookResponse> top3(@RequestParam Genre genre) {
        return mapper.toResponseList(service.getRepository().findTop3ByGenreOrderByYearDesc(genre));
    }

    @Operation(summary = "Archive books older than a given year (bulk update)")
    @ApiResponse(responseCode = "200", description = "Number of books archived")
    @PostMapping("/archive")
    public int archiveOlderThan(@RequestParam int year) {
        return service.archiveOlderThan(year);
    }

    @Operation(summary = "Find a book by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was found"),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public BookResponse find(
            @Parameter(description = "Identifier of the book", example = "42")
            @PathVariable long id
    ) {
        return mapper.toResponse(service.findById(id));
    }

    @Operation(summary = "Create a new book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Book title already exists",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<BookResponse> create(
            @Valid @RequestBody BookRequest request
    ) {
        Book saved = service.create(mapper.toDomain(request));
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.getId()))
                .body(mapper.toResponse(saved));
    }

    @Operation(summary = "Update an existing book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public BookResponse update(
            @Parameter(description = "Identifier of the book", example = "1")
            @PathVariable long id,
            @Valid @RequestBody BookRequest request
    ) {
        return mapper.toResponse(service.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Delete a book by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The book was deleted"),
            @ApiResponse(responseCode = "404", description = "No book with this id",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifier of the book", example = "1")
            @PathVariable long id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
