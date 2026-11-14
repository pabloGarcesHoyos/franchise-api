package co.com.accenture.api.dto;

import co.com.accenture.model.common.NameRules;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product to add to a branch")
public record ProductRequest(
        @Schema(description = "Product name, unique within the branch and trimmed before it is stored",
                example = "Pepperoni", requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 1, maxLength = NameRules.MAX_LENGTH)
        String name,
        @Schema(description = "Units in stock", example = "15", requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "0")
        Integer stock) {
}
