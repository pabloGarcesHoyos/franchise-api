package co.com.accenture.config;

import co.com.accenture.model.branch.gateways.BranchRepository;
import co.com.accenture.model.franchise.gateways.FranchiseRepository;
import co.com.accenture.model.product.gateways.ProductRepository;
import co.com.accenture.usecase.addbranch.AddBranchUseCase;
import co.com.accenture.usecase.addproduct.AddProductUseCase;
import co.com.accenture.usecase.createfranchise.CreateFranchiseUseCase;
import co.com.accenture.usecase.deleteproduct.DeleteProductUseCase;
import co.com.accenture.usecase.gettopstockproducts.GetTopStockProductsUseCase;
import co.com.accenture.usecase.renamebranch.RenameBranchUseCase;
import co.com.accenture.usecase.renamefranchise.RenameFranchiseUseCase;
import co.com.accenture.usecase.renameproduct.RenameProductUseCase;
import co.com.accenture.usecase.updateproductstock.UpdateProductStockUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    private static final List<Class<?>> USE_CASES = List.of(
            CreateFranchiseUseCase.class,
            RenameFranchiseUseCase.class,
            AddBranchUseCase.class,
            RenameBranchUseCase.class,
            AddProductUseCase.class,
            DeleteProductUseCase.class,
            UpdateProductStockUseCase.class,
            RenameProductUseCase.class,
            GetTopStockProductsUseCase.class);

    @Test
    void shouldRegisterEveryUseCaseWiredToTheGateways() {
        try (AnnotationConfigApplicationContext context = contextWithGateways()) {
            USE_CASES.forEach(type -> assertThat(context.getBean(type)).isNotNull());

            List<String> useCaseBeans = Arrays.stream(context.getBeanDefinitionNames())
                    .filter(name -> name.endsWith("UseCase"))
                    .toList();
            assertThat(useCaseBeans).hasSize(USE_CASES.size());
        }
    }

    private static AnnotationConfigApplicationContext contextWithGateways() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(FranchiseRepository.class, () -> mock(FranchiseRepository.class));
        context.registerBean(BranchRepository.class, () -> mock(BranchRepository.class));
        context.registerBean(ProductRepository.class, () -> mock(ProductRepository.class));
        context.register(UseCasesConfig.class);
        context.refresh();
        return context;
    }
}
