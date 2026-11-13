package co.com.accenture.model.branch;

import co.com.accenture.model.common.NameRules;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Branch {

    private final String id;
    private final String franchiseId;
    private final String name;

    public static Branch create(String franchiseId, String name) {
        return Branch.builder()
                .franchiseId(franchiseId)
                .name(NameRules.normalize(name))
                .build();
    }

    public Branch rename(String name) {
        return toBuilder()
                .name(NameRules.normalize(name))
                .build();
    }
}
