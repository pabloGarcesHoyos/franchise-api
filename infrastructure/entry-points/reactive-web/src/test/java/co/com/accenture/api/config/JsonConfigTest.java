package co.com.accenture.api.config;

import co.com.accenture.api.dto.NameRequest;
import co.com.accenture.api.dto.StockRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonConfigTest {

    private final JsonMapper mapper = strictMapper();

    @Test
    void shouldReadTextualNameAndIntegerStock() {
        assertThat(mapper.readValue("{\"name\":\"Burger\"}", NameRequest.class).name()).isEqualTo("Burger");
        assertThat(mapper.readValue("{\"stock\":10}", StockRequest.class).stock()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "1.5", "true", "false"})
    void shouldRejectNonTextualName(String value) {
        assertThatThrownBy(() -> mapper.readValue("{\"name\":" + value + "}", NameRequest.class))
                .isInstanceOf(DatabindException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "1.0", "\"10\"", "true"})
    void shouldRejectStockThatIsNotAJsonInteger(String value) {
        assertThatThrownBy(() -> mapper.readValue("{\"stock\":" + value + "}", StockRequest.class))
                .isInstanceOf(DatabindException.class);
    }

    private static JsonMapper strictMapper() {
        JsonMapper.Builder builder = JsonMapper.builder();
        new JsonConfig().strictScalarCoercion().customize(builder);
        return builder.build();
    }
}
