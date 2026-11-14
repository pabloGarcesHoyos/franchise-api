package co.com.accenture.api.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Uniform error body")
public record ErrorResponse(
        @Schema(description = "Stable error code", example = "FRANCHISE_NOT_FOUND") String code,
        @Schema(description = "Human readable message", example = "Franchise not found") String message) {
}
