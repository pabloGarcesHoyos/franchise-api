package co.com.accenture.model.common;

import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;

public final class NameRules {

    public static final int MAX_LENGTH = 100;

    private NameRules() {
    }

    public static String normalize(String name) {
        if (name == null) {
            throw new BusinessException(BusinessError.INVALID_NAME);
        }
        String trimmed = trim(name);
        if (trimmed.isEmpty()) {
            throw new BusinessException(BusinessError.INVALID_NAME);
        }
        if (trimmed.codePointCount(0, trimmed.length()) > MAX_LENGTH) {
            throw new BusinessException(BusinessError.NAME_TOO_LONG);
        }
        return trimmed;
    }

    private static String trim(String name) {
        int start = 0;
        int end = name.length();
        while (start < end && isBlank(name.codePointAt(start))) {
            start += Character.charCount(name.codePointAt(start));
        }
        while (end > start && isBlank(name.codePointBefore(end))) {
            end -= Character.charCount(name.codePointBefore(end));
        }
        return name.substring(start, end);
    }

    private static boolean isBlank(int codePoint) {
        return Character.isWhitespace(codePoint)
                || Character.isSpaceChar(codePoint)
                || Character.isISOControl(codePoint)
                || Character.getType(codePoint) == Character.FORMAT;
    }
}
