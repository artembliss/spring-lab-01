package kz.iitu.springlab.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated response wrapper")
public record PagedResponse<T>(
        @Schema(description = "Items of the current page")
        List<T> content,

        @Schema(description = "Current page index (0-based)", example = "0")
        int page,

        @Schema(description = "Page size", example = "5")
        int size,

        @Schema(description = "Total number of elements across all pages", example = "10")
        long totalElements,

        @Schema(description = "Total number of pages", example = "2")
        int totalPages
) {
}
