package co.com.accenture.api.dto;

import co.com.accenture.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product of a branch")
public record ProductResponse(
        @Schema(description = "Product identifier", example = "6af7a0b1c2d3e4f5a6b7c8db") String id,
        @Schema(description = "Identifier of the branch the product belongs to", example = "6af7a0b1c2d3e4f5a6b7c8da")
        String branchId,
        @Schema(description = "Product name", example = "Pepperoni") String name,
        @Schema(description = "Units in stock", example = "15", minimum = "0") int stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }
}
