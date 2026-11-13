package co.com.accenture.mongo.product;

import co.com.accenture.model.product.Product;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@CompoundIndex(name = "branch_id_name_unique", def = "{'branchId': 1, 'name': 1}", unique = true)
public record ProductDocument(@Id String id, String branchId, String name, int stock) {

    public static ProductDocument fromDomain(Product product) {
        return new ProductDocument(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }

    public static Product toDomain(ProductDocument document) {
        return Product.builder()
                .id(document.id())
                .branchId(document.branchId())
                .name(document.name())
                .stock(document.stock())
                .build();
    }
}
