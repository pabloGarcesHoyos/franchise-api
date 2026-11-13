package co.com.accenture.api.product;

import co.com.accenture.api.config.JsonConfig;
import co.com.accenture.api.error.GlobalErrorHandler;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.product.Product;
import co.com.accenture.usecase.addproduct.AddProductUseCase;
import co.com.accenture.usecase.deleteproduct.DeleteProductUseCase;
import co.com.accenture.usecase.renameproduct.RenameProductUseCase;
import co.com.accenture.usecase.updateproductstock.UpdateProductStockUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ProductRouter.class, ProductHandler.class, GlobalErrorHandler.class, JsonConfig.class})
class ProductRouterTest {

    private static final String PRODUCT_JSON = "{\"id\":\"p1\",\"branchId\":\"b1\",\"name\":\"Burger\",\"stock\":10}";

    private final Product product = Product.builder().id("p1").branchId("b1").name("Burger").stock(10).build();

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private AddProductUseCase addProductUseCase;

    @MockitoBean
    private DeleteProductUseCase deleteProductUseCase;

    @MockitoBean
    private UpdateProductStockUseCase updateProductStockUseCase;

    @MockitoBean
    private RenameProductUseCase renameProductUseCase;

    @Test
    void shouldAddProductAndReturn201() {
        when(addProductUseCase.execute("b1", "Burger", 10)).thenReturn(Mono.just(product));

        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\",\"stock\":10}")
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(PRODUCT_JSON, true);
    }

    @Test
    void shouldReturn404WhenAddingToMissingBranch() {
        when(addProductUseCase.execute("b1", "Burger", 10))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NOT_FOUND)));

        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\",\"stock\":10}")
                .expectStatus().isNotFound()
                .expectBody().json("{\"code\":\"BRANCH_NOT_FOUND\",\"message\":\"Branch not found\"}", true);
    }

    @Test
    void shouldPassMissingStockToUseCaseAndReturn400() {
        when(addProductUseCase.execute(eq("b1"), eq("Burger"), isNull()))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_STOCK)));

        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\"}")
                .expectStatus().isBadRequest()
                .expectBody().json("{\"code\":\"INVALID_STOCK\","
                        + "\"message\":\"Stock is required and must be greater than or equal to zero\"}", true);
    }

    @Test
    void shouldPassMissingNameToUseCaseAndReturn400() {
        when(addProductUseCase.execute(eq("b1"), isNull(), eq(10)))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        send(client.post(), "/api/branches/b1/products", "{\"stock\":10}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_NAME");
    }

    @Test
    void shouldReturn409WhenProductNameExistsInBranch() {
        when(addProductUseCase.execute("b1", "Burger", 10))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NAME_ALREADY_EXISTS)));

        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\",\"stock\":10}")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("PRODUCT_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn400ForMalformedJsonWithoutCallingUseCase() {
        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\",")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(addProductUseCase, never()).execute(anyString(), any(), any());
    }

    @Test
    void shouldReturn400ForNonNumericStockWithoutCallingUseCase() {
        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\",\"stock\":\"abc\"}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(addProductUseCase, never()).execute(anyString(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "\"10\"", "true"})
    void shouldRejectStockThatIsNotAJsonIntegerWithoutCallingUseCase(String stock) {
        send(client.post(), "/api/branches/b1/products", "{\"name\":\"Burger\",\"stock\":" + stock + "}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(addProductUseCase, never()).execute(anyString(), any(), any());
    }

    @Test
    void shouldRejectDecimalStockOnUpdateWithoutCallingUseCase() {
        send(client.patch(), "/api/products/p1/stock", "{\"stock\":2.7}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(updateProductStockUseCase, never()).execute(anyString(), any());
    }

    @Test
    void shouldReturn400ForEmptyBodyWithoutCallingUseCase() {
        client.post().uri("/api/branches/b1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(addProductUseCase, never()).execute(anyString(), any(), any());
    }

    @Test
    void shouldDeleteProductAndReturn204() {
        when(deleteProductUseCase.execute("b1", "p1")).thenReturn(Mono.empty());

        client.delete().uri("/api/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(deleteProductUseCase).execute("b1", "p1");
    }

    @Test
    void shouldReturn404WhenDeletingFromMissingBranch() {
        when(deleteProductUseCase.execute("b1", "p1"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.BRANCH_NOT_FOUND)));

        client.delete().uri("/api/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("BRANCH_NOT_FOUND");
    }

    @Test
    void shouldReturn404WhenDeletingProductOutsideBranch() {
        when(deleteProductUseCase.execute("b1", "p1"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NOT_FOUND)));

        client.delete().uri("/api/branches/b1/products/p1")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().json("{\"code\":\"PRODUCT_NOT_FOUND\",\"message\":\"Product not found\"}", true);
    }

    @Test
    void shouldUpdateStockAndReturn200() {
        Product updated = product.toBuilder().stock(25).build();
        when(updateProductStockUseCase.execute("p1", 25)).thenReturn(Mono.just(updated));

        send(client.patch(), "/api/products/p1/stock", "{\"stock\":25}")
                .expectStatus().isOk()
                .expectBody().json("{\"id\":\"p1\",\"branchId\":\"b1\",\"name\":\"Burger\",\"stock\":25}", true);
    }

    @Test
    void shouldPassNullStockToUseCaseAndReturn400() {
        when(updateProductStockUseCase.execute(eq("p1"), isNull()))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_STOCK)));

        send(client.patch(), "/api/products/p1/stock", "{\"stock\":null}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_STOCK");
    }

    @Test
    void shouldReturn400ForNegativeStock() {
        when(updateProductStockUseCase.execute("p1", -1))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_STOCK)));

        send(client.patch(), "/api/products/p1/stock", "{\"stock\":-1}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_STOCK");
    }

    @Test
    void shouldReturn404WhenUpdatingStockOfMissingProduct() {
        when(updateProductStockUseCase.execute("p1", 25))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NOT_FOUND)));

        send(client.patch(), "/api/products/p1/stock", "{\"stock\":25}")
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void shouldReturn400ForEmptyStockBodyWithoutCallingUseCase() {
        client.patch().uri("/api/products/p1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(updateProductStockUseCase, never()).execute(anyString(), any());
    }

    @Test
    void shouldRenameProductAndReturn200() {
        Product renamed = product.toBuilder().name("Cheeseburger").build();
        when(renameProductUseCase.execute("p1", "Cheeseburger")).thenReturn(Mono.just(renamed));

        send(client.patch(), "/api/products/p1/name", "{\"name\":\"Cheeseburger\"}")
                .expectStatus().isOk()
                .expectBody().json("{\"id\":\"p1\",\"branchId\":\"b1\",\"name\":\"Cheeseburger\",\"stock\":10}", true);
    }

    @Test
    void shouldReturn404WhenRenamingMissingProduct() {
        when(renameProductUseCase.execute("p1", "Cheeseburger"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NOT_FOUND)));

        send(client.patch(), "/api/products/p1/name", "{\"name\":\"Cheeseburger\"}")
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void shouldReturn400WhenRenamingWithBlankName() {
        when(renameProductUseCase.execute("p1", "  "))
                .thenReturn(Mono.error(new BusinessException(BusinessError.INVALID_NAME)));

        send(client.patch(), "/api/products/p1/name", "{\"name\":\"  \"}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_NAME");
    }

    @Test
    void shouldReturn409WhenRenamingToExistingName() {
        when(renameProductUseCase.execute("p1", "Taken"))
                .thenReturn(Mono.error(new BusinessException(BusinessError.PRODUCT_NAME_ALREADY_EXISTS)));

        send(client.patch(), "/api/products/p1/name", "{\"name\":\"Taken\"}")
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("PRODUCT_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn400ForMalformedJsonOnRenameWithoutCallingUseCase() {
        send(client.patch(), "/api/products/p1/name", "[")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(renameProductUseCase, never()).execute(anyString(), any());
    }

    private WebTestClient.ResponseSpec send(WebTestClient.RequestBodyUriSpec method, String uri, String body) {
        return method.uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }
}
