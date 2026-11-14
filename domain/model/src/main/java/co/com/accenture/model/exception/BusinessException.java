package co.com.accenture.model.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BusinessError error;

    public BusinessException(BusinessError error) {
        super(error.getMessage());
        this.error = error;
    }
}
