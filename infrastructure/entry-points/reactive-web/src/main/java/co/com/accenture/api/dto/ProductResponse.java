package co.com.accenture.api.dto;

import co.com.accenture.model.product.Product;

public record ProductResponse(String id, String branchId, String name, int stock) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }
}
