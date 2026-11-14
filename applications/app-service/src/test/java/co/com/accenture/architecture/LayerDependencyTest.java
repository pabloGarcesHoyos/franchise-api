package co.com.accenture.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class LayerDependencyTest {

    private static final String MODEL = "co.com.accenture.model..";
    private static final String USE_CASE = "co.com.accenture.usecase..";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("co.com.accenture");

    @Test
    void layersShouldOnlyDependInwardsTowardsTheDomain() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Model").definedBy(MODEL)
                .layer("UseCase").definedBy(USE_CASE)
                .layer("EntryPoint").definedBy("co.com.accenture.api..")
                .layer("DrivenAdapter").definedBy("co.com.accenture.mongo..")
                .layer("Application").definedBy("co.com.accenture", "co.com.accenture.config..")
                .whereLayer("EntryPoint").mayNotBeAccessedByAnyLayer()
                .whereLayer("DrivenAdapter").mayNotBeAccessedByAnyLayer()
                .whereLayer("UseCase").mayOnlyBeAccessedByLayers("EntryPoint", "Application")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("UseCase", "EntryPoint", "DrivenAdapter", "Application")
                .check(CLASSES);
    }

    @Test
    void modelShouldOnlyDependOnJavaLombokAndReactor() {
        classes().that().resideInAPackage(MODEL)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "lombok..", "reactor.core.publisher..", MODEL)
                .check(CLASSES);
    }

    @Test
    void useCasesShouldOnlyDependOnTheModel() {
        classes().that().resideInAPackage(USE_CASE)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "lombok..", "reactor.core.publisher..", MODEL, USE_CASE)
                .check(CLASSES);
    }
}
