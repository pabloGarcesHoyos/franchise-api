package co.com.accenture.mongo.franchise;

import co.com.accenture.model.franchise.Franchise;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FranchiseDocumentTest {

    @Test
    void shouldMapFromDomain() {
        Franchise franchise = Franchise.builder().id("f1").name("Burger House").build();

        assertThat(FranchiseDocument.fromDomain(franchise))
                .isEqualTo(new FranchiseDocument("f1", "Burger House"));
    }

    @Test
    void shouldMapToDomain() {
        Franchise franchise = FranchiseDocument.toDomain(new FranchiseDocument("f1", "Burger House"));

        assertThat(franchise.getId()).isEqualTo("f1");
        assertThat(franchise.getName()).isEqualTo("Burger House");
    }
}
