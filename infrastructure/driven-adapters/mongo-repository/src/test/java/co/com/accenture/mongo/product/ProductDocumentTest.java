package co.com.accenture.mongo.product;

import co.com.accenture.model.product.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDocumentTest {

    @Test
    void shouldMapFromDomain() {
        Product product = Product.builder().id("p1").branchId("b1").name("Burger").stock(10).build();

        assertThat(ProductDocument.fromDomain(product))
                .isEqualTo(new ProductDocument("p1", "b1", "Burger", 10));
    }

    @Test
    void shouldMapToDomain() {
        Product product = ProductDocument.toDomain(new ProductDocument("p1", "b1", "Burger", 10));

        assertThat(product.getId()).isEqualTo("p1");
        assertThat(product.getBranchId()).isEqualTo("b1");
        assertThat(product.getName()).isEqualTo("Burger");
        assertThat(product.getStock()).isEqualTo(10);
    }
}
