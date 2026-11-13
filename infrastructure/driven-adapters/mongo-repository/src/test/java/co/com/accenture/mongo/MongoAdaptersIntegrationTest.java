package co.com.accenture.mongo;

import co.com.accenture.model.branch.Branch;
import co.com.accenture.model.exception.BusinessError;
import co.com.accenture.model.exception.BusinessException;
import co.com.accenture.model.franchise.Franchise;
import co.com.accenture.model.product.Product;
import co.com.accenture.mongo.branch.BranchDocument;
import co.com.accenture.mongo.branch.BranchRepositoryAdapter;
import co.com.accenture.mongo.franchise.FranchiseDocument;
import co.com.accenture.mongo.franchise.FranchiseRepositoryAdapter;
import co.com.accenture.mongo.product.ProductDocument;
import co.com.accenture.mongo.product.ProductRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
@DataMongoTest(properties = "spring.data.mongodb.auto-index-creation=true")
@Import({FranchiseRepositoryAdapter.class, BranchRepositoryAdapter.class, ProductRepositoryAdapter.class})
class MongoAdaptersIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.0");

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private FranchiseRepositoryAdapter franchises;

    @Autowired
    private BranchRepositoryAdapter branches;

    @Autowired
    private ProductRepositoryAdapter products;

    @BeforeEach
    void cleanCollectionsAndWaitForIndexes() {
        Flux.just(FranchiseDocument.class, BranchDocument.class, ProductDocument.class)
                .concatMap(type -> template.remove(new Query(), type))
                .blockLast();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(uniqueIndexFields(FranchiseDocument.class)).contains(List.of("name"));
            assertThat(uniqueIndexFields(BranchDocument.class)).contains(List.of("franchiseId", "name"));
            assertThat(uniqueIndexFields(ProductDocument.class)).contains(List.of("branchId", "name"));
        });
    }

    @Test
    void shouldPersistFranchiseWithGeneratedIdAndRejectDuplicateName() {
        StepVerifier.create(franchises.save(Franchise.create("Burger House"))
                        .flatMap(saved -> franchises.findById(saved.getId())))
                .assertNext(found -> {
                    assertThat(found.getId()).isNotBlank();
                    assertThat(found.getName()).isEqualTo("Burger House");
                })
                .verifyComplete();

        StepVerifier.create(franchises.save(Franchise.create("Burger House")))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.FRANCHISE_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldRejectDuplicateBranchNameOnlyWithinSameFranchise() {
        StepVerifier.create(branches.save(Branch.create("f1", "Downtown"))
                        .then(branches.save(Branch.create("f2", "Downtown"))))
                .assertNext(saved -> assertThat(saved.getFranchiseId()).isEqualTo("f2"))
                .verifyComplete();

        StepVerifier.create(branches.save(Branch.create("f1", "Downtown")))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldRejectRenamingBranchToExistingNameInSameFranchise() {
        StepVerifier.create(branches.save(Branch.create("f1", "Downtown"))
                        .then(branches.save(Branch.create("f1", "Airport")))
                        .flatMap(airport -> branches.save(airport.rename("Downtown"))))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.BRANCH_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldRejectDuplicateProductNameOnlyWithinSameBranch() {
        StepVerifier.create(products.save(Product.create("b1", "Burger", 10))
                        .then(products.save(Product.create("b2", "Burger", 5))))
                .assertNext(saved -> assertThat(saved.getBranchId()).isEqualTo("b2"))
                .verifyComplete();

        StepVerifier.create(products.save(Product.create("b1", "Burger", 3)))
                .expectErrorSatisfies(error -> assertThat(((BusinessException) error).getError())
                        .isEqualTo(BusinessError.PRODUCT_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    void shouldReturnBranchesOfFranchiseOrderedByName() {
        StepVerifier.create(Flux.concat(
                                branches.save(Branch.create("f1", "Zeta")),
                                branches.save(Branch.create("f1", "Alpha")),
                                branches.save(Branch.create("f2", "Beta")),
                                branches.save(Branch.create("f1", "Mid")))
                        .thenMany(branches.findByFranchiseId("f1"))
                        .map(Branch::getName))
                .expectNext("Alpha", "Mid", "Zeta")
                .verifyComplete();
    }

    @Test
    void shouldReturnTopStockProductOfBranchBreakingTiesByName() {
        StepVerifier.create(Flux.concat(
                                products.save(Product.create("b1", "Soda", 10)),
                                products.save(Product.create("b1", "Fries", 30)),
                                products.save(Product.create("b1", "Burger", 30)),
                                products.save(Product.create("b2", "Pizza", 99)))
                        .then(products.findTopByStock("b1")))
                .assertNext(top -> {
                    assertThat(top.getName()).isEqualTo("Burger");
                    assertThat(top.getStock()).isEqualTo(30);
                    assertThat(top.getBranchId()).isEqualTo("b1");
                })
                .verifyComplete();

        StepVerifier.create(products.findTopByStock("b3"))
                .verifyComplete();
    }

    @Test
    void shouldFindProductOnlyInItsBranchAndDeleteIt() {
        String productId = products.save(Product.create("b1", "Burger", 10)).map(Product::getId).block();

        StepVerifier.create(products.findByIdAndBranchId(productId, "b2"))
                .verifyComplete();

        StepVerifier.create(products.findByIdAndBranchId(productId, "b1"))
                .assertNext(found -> assertThat(found.getName()).isEqualTo("Burger"))
                .verifyComplete();

        StepVerifier.create(products.deleteById(productId).then(products.findById(productId)))
                .verifyComplete();
    }

    private List<List<String>> uniqueIndexFields(Class<?> type) {
        return template.indexOps(type).getIndexInfo()
                .filter(IndexInfo::isUnique)
                .map(index -> index.getIndexFields().stream().map(field -> field.getKey()).toList())
                .collectList()
                .block();
    }
}
