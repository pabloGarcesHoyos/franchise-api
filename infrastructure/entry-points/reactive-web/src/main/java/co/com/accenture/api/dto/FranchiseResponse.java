package co.com.accenture.api.dto;

import co.com.accenture.model.franchise.Franchise;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Franchise")
public record FranchiseResponse(
        @Schema(description = "Franchise identifier", example = "6af7a0b1c2d3e4f5a6b7c8d9") String id,
        @Schema(description = "Franchise name", example = "Pizza Palace") String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.getId(), franchise.getName());
    }
}
