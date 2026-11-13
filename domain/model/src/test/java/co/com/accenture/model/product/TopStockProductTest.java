package co.com.accenture.model.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopStockProductTest {

    @Test
    void shouldExposeBranchAndProductData() {
        TopStockProduct top = new TopStockProduct("b1", "Downtown", "p1", "Burger", 30);

        assertThat(top.branchId()).isEqualTo("b1");
        assertThat(top.branchName()).isEqualTo("Downtown");
        assertThat(top.productId()).isEqualTo("p1");
        assertThat(top.productName()).isEqualTo("Burger");
        assertThat(top.stock()).isEqualTo(30);
    }

    @Test
    void shouldBeEqualWhenAllComponentsMatch() {
        assertThat(new TopStockProduct("b1", "Downtown", "p1", "Burger", 30))
                .isEqualTo(new TopStockProduct("b1", "Downtown", "p1", "Burger", 30));
    }
}
