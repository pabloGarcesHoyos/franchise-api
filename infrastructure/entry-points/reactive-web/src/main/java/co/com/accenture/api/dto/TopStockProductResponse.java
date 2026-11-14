package co.com.accenture.api.dto;

import co.com.accenture.model.product.TopStockProduct;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product with the most stock in a branch")
public record TopStockProductResponse(
        @Schema(description = "Branch identifier", example = "6af7a0b1c2d3e4f5a6b7c8da") String branchId,
        @Schema(description = "Branch name", example = "Airport") String branchName,
        @Schema(description = "Product identifier", example = "6af7a0b1c2d3e4f5a6b7c8db") String productId,
        @Schema(description = "Product name", example = "Pepperoni") String productName,
        @Schema(description = "Units in stock", example = "35", minimum = "0") int stock) {

    public static TopStockProductResponse from(TopStockProduct topStockProduct) {
        return new TopStockProductResponse(
                topStockProduct.branchId(),
                topStockProduct.branchName(),
                topStockProduct.productId(),
                topStockProduct.productName(),
                topStockProduct.stock());
    }
}
