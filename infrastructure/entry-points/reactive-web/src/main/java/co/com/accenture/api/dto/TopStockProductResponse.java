package co.com.accenture.api.dto;

import co.com.accenture.model.product.TopStockProduct;

public record TopStockProductResponse(String branchId, String branchName, String productId, String productName,
                                      int stock) {

    public static TopStockProductResponse from(TopStockProduct topStockProduct) {
        return new TopStockProductResponse(
                topStockProduct.branchId(),
                topStockProduct.branchName(),
                topStockProduct.productId(),
                topStockProduct.productName(),
                topStockProduct.stock());
    }
}
