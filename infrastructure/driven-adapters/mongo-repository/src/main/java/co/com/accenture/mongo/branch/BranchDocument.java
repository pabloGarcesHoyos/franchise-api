package co.com.accenture.mongo.branch;

import co.com.accenture.model.branch.Branch;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "branches")
@CompoundIndex(name = "franchise_id_name_unique", def = "{'franchiseId': 1, 'name': 1}", unique = true)
public record BranchDocument(@Id String id, String franchiseId, String name) {

    public static BranchDocument fromDomain(Branch branch) {
        return new BranchDocument(branch.getId(), branch.getFranchiseId(), branch.getName());
    }

    public static Branch toDomain(BranchDocument document) {
        return Branch.builder()
                .id(document.id())
                .franchiseId(document.franchiseId())
                .name(document.name())
                .build();
    }
}
