package co.com.accenture.api.product;

import co.com.accenture.api.RequestBodies;
import co.com.accenture.api.dto.NameRequest;
import co.com.accenture.api.dto.ProductRequest;
import co.com.accenture.api.dto.ProductResponse;
import co.com.accenture.api.dto.StockRequest;
import co.com.accenture.model.product.Product;
import co.com.accenture.usecase.addproduct.AddProductUseCase;
import co.com.accenture.usecase.deleteproduct.DeleteProductUseCase;
import co.com.accenture.usecase.renameproduct.RenameProductUseCase;
import co.com.accenture.usecase.updateproductstock.UpdateProductStockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final AddProductUseCase addProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final RenameProductUseCase renameProductUseCase;

    public Mono<ServerResponse> add(ServerRequest request) {
        String branchId = request.pathVariable("branchId");
        return RequestBodies.required(request, ProductRequest.class)
                .flatMap(body -> addProductUseCase.execute(branchId, body.name(), body.stock()))
                .map(ProductResponse::from)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return deleteProductUseCase.execute(request.pathVariable("branchId"), request.pathVariable("productId"))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        String productId = request.pathVariable("productId");
        return RequestBodies.required(request, StockRequest.class)
                .flatMap(body -> updateProductStockUseCase.execute(productId, body.stock()))
                .flatMap(this::ok);
    }

    public Mono<ServerResponse> rename(ServerRequest request) {
        String productId = request.pathVariable("productId");
        return RequestBodies.required(request, NameRequest.class)
                .flatMap(body -> renameProductUseCase.execute(productId, body.name()))
                .flatMap(this::ok);
    }

    private Mono<ServerResponse> ok(Product product) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ProductResponse.from(product));
    }
}
