package co.com.accenture.mongo.franchise;

import co.com.accenture.model.franchise.Franchise;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "franchises")
public record FranchiseDocument(@Id String id, @Indexed(unique = true) String name) {

    public static FranchiseDocument fromDomain(Franchise franchise) {
        return new FranchiseDocument(franchise.getId(), franchise.getName());
    }

    public static Franchise toDomain(FranchiseDocument document) {
        return Franchise.builder()
                .id(document.id())
                .name(document.name())
                .build();
    }
}
