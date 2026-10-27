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
import kz.iitu.springlab.web.dto.PagedResponse;
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

    @Operation(summary = "List all books with optional filtering or paging")
    @ApiResponse(responseCode = "200", description = "Collection of books or paged response")
    @GetMapping
    public Object list(
            @Parameter(description = "Filter by author name")
            @RequestParam(required = false) String author,
            @Parameter(description = "Limit number of returned items")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Page number (0-based) for pagination (Variant 1)")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size for pagination (Variant 1)")
            @RequestParam(defaultValue = "10") int size
    ) {
        List<BookResponse> responses = mapper.toResponseList(service.findAll(author));

        if (page != null) {
            int pageSize = Math.max(1, size);
            int pageIndex = Math.max(0, page);
            int totalElements = responses.size();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            int fromIndex = Math.min(pageIndex * pageSize, totalElements);
            int toIndex = Math.min(fromIndex + pageSize, totalElements);
            List<BookResponse> pageContent = responses.subList(fromIndex, toIndex);
            return new PagedResponse<>(pageContent, pageIndex, pageSize, totalElements, totalPages);
        }

        if (limit != null && limit >= 0) {
            return responses.stream().limit(limit).toList();
        }

        return responses;
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
                .created(URI.create("/api/books/" + saved.id()))
                .body(mapper.toResponse(saved));
    }

    @Operation(summary = "Update an existing book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The book was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
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
