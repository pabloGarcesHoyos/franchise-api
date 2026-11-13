package co.com.accenture.model.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void shouldExposeErrorAndUseItsMessage() {
        BusinessException exception = new BusinessException(BusinessError.FRANCHISE_NOT_FOUND);

        assertThat(exception.getError()).isEqualTo(BusinessError.FRANCHISE_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("Franchise not found");
        assertThat(exception.getError().getCode()).isEqualTo("FRANCHISE_NOT_FOUND");
        assertThat(exception.getError().getType()).isEqualTo(ErrorType.NOT_FOUND);
    }
}
