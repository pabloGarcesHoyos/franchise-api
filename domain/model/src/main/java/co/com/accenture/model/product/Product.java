package co.com.accenture.model.product;

import co.com.accenture.model.common.NameRules;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Product {

    private final String id;
    private final String branchId;
    private final String name;
    private final int stock;

    public static Product create(String branchId, String name, Integer stock) {
        return Product.builder()
                .branchId(branchId)
                .name(NameRules.normalize(name))
                .stock(requireValidStock(stock))
                .build();
    }

    public Product rename(String name) {
        return toBuilder()
                .name(NameRules.normalize(name))
                .build();
    }

    public Product updateStock(Integer stock) {
        return toBuilder()
                .stock(requireValidStock(stock))
                .build();
    }

    private static int requireValidStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new BusinessException(BusinessError.INVALID_STOCK);
        }
        return stock;
    }
}
