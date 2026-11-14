package co.com.accenture.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New stock of the product")
public record StockRequest(
        @Schema(description = "Units in stock", example = "35", requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0")
        Integer stock) {
}
