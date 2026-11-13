package co.com.accenture.api.config;

import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldDescribeApiTitleDescriptionAndVersion() {
        Info info = new OpenApiConfig().franchiseOpenApi().getInfo();

        assertThat(info.getTitle()).isEqualTo("Franchise API");
        assertThat(info.getDescription()).isNotBlank();
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }
}
