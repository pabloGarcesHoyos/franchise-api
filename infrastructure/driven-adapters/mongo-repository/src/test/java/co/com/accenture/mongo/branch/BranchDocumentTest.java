package co.com.accenture.mongo.branch;

import co.com.accenture.model.branch.Branch;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BranchDocumentTest {

    @Test
    void shouldMapFromDomain() {
        Branch branch = Branch.builder().id("b1").franchiseId("f1").name("Downtown").build();

        assertThat(BranchDocument.fromDomain(branch))
                .isEqualTo(new BranchDocument("b1", "f1", "Downtown"));
    }

    @Test
    void shouldMapToDomain() {
        Branch branch = BranchDocument.toDomain(new BranchDocument("b1", "f1", "Downtown"));

        assertThat(branch.getId()).isEqualTo("b1");
        assertThat(branch.getFranchiseId()).isEqualTo("f1");
        assertThat(branch.getName()).isEqualTo("Downtown");
    }
}
