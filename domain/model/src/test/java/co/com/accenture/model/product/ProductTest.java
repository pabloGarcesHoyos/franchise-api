package co.com.accenture.model.product;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private final Product existing = Product.builder().id("p1").branchId("b1").name("Old").stock(5).build();

    @Test
    void shouldCreateProductWithStrippedNameStockAndNoId() {
        Product product = Product.create("b1", "  Burger  ", 10);

        assertThat(product.getId()).isNull();
        assertThat(product.getBranchId()).isEqualTo("b1");
        assertThat(product.getName()).isEqualTo("Burger");
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void shouldAllowZeroStockOnCreate() {
        assertThat(Product.create("b1", "Burger", 0).getStock()).isZero();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectInvalidNameOnCreate(String name) {
        assertThatThrownBy(() -> Product.create("b1", name, 1))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void shouldRejectNegativeStockOnCreate(int stock) {
        assertThatThrownBy(() -> Product.create("b1", "Burger", stock))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_STOCK);
    }

    @Test
    void shouldReturnRenamedCopyKeepingOtherFieldsAndOriginalUnchanged() {
        Product renamed = existing.rename("  New  ");

        assertThat(renamed).isNotSameAs(existing);
        assertThat(renamed.getId()).isEqualTo("p1");
        assertThat(renamed.getBranchId()).isEqualTo("b1");
        assertThat(renamed.getName()).isEqualTo("New");
        assertThat(renamed.getStock()).isEqualTo(5);
        assertThat(existing.getName()).isEqualTo("Old");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectInvalidNameOnRename(String name) {
        assertThatThrownBy(() -> existing.rename(name))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_NAME);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 250})
    void shouldReturnCopyWithUpdatedStockKeepingOtherFieldsAndOriginalUnchanged(int stock) {
        Product updated = existing.updateStock(stock);

        assertThat(updated).isNotSameAs(existing);
        assertThat(updated.getId()).isEqualTo("p1");
        assertThat(updated.getBranchId()).isEqualTo("b1");
        assertThat(updated.getName()).isEqualTo("Old");
        assertThat(updated.getStock()).isEqualTo(stock);
        assertThat(existing.getStock()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MIN_VALUE})
    void shouldRejectNegativeStockOnUpdate(int stock) {
        assertThatThrownBy(() -> existing.updateStock(stock))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getError())
                .isEqualTo(BusinessError.INVALID_STOCK);
    }
}
