package co.com.accenture.api.dto;

import co.com.accenture.model.common.NameRules;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New name of the resource")
public record NameRequest(
        @Schema(description = "Name, trimmed before it is stored", example = "Pizza Palace",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = NameRules.MAX_LENGTH)
        String name) {
}
