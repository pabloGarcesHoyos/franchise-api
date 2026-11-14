package co.com.accenture.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessError {
    INVALID_NAME("INVALID_NAME", "Name must not be empty", ErrorType.VALIDATION),
    INVALID_STOCK("INVALID_STOCK", "Stock is required and must be greater than or equal to zero", ErrorType.VALIDATION),
    FRANCHISE_NOT_FOUND("FRANCHISE_NOT_FOUND", "Franchise not found", ErrorType.NOT_FOUND),
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Branch not found", ErrorType.NOT_FOUND),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found", ErrorType.NOT_FOUND),
    FRANCHISE_NAME_ALREADY_EXISTS("FRANCHISE_NAME_ALREADY_EXISTS", "A franchise with this name already exists", ErrorType.CONFLICT),
    BRANCH_NAME_ALREADY_EXISTS("BRANCH_NAME_ALREADY_EXISTS", "A branch with this name already exists in the franchise", ErrorType.CONFLICT),
    PRODUCT_NAME_ALREADY_EXISTS("PRODUCT_NAME_ALREADY_EXISTS", "A product with this name already exists in the branch", ErrorType.CONFLICT);

    private final String code;
    private final String message;
    private final ErrorType type;
}
