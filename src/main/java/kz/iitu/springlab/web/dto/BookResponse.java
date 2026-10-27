package kz.iitu.springlab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import kz.iitu.springlab.web.Genre;

@Schema(description = "Response representation of a book resource")
public record BookResponse(
        @Schema(description = "Unique identifier of the book", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "Effective Java")
        String title,

        @Schema(description = "Author name", example = "Joshua Bloch")
        String author,

        @Schema(description = "Publication year", example = "2018")
        int year,

        @Schema(description = "Book genre", example = "TECHNOLOGY")
        Genre genre,

        @Schema(description = "Timestamp when the book was registered")
        Instant createdAt
) {
}
