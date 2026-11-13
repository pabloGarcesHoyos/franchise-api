package co.com.accenture.model.common;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;

public final class NameRules {

    private NameRules() {
    }

    public static String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(BusinessError.INVALID_NAME);
        }
        return name.strip();
    }
}
