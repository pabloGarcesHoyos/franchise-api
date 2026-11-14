package co.com.accenture.model.franchise;

import co.com.accenture.model.common.NameRules;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Franchise {

    private final String id;
    private final String name;

    public static Franchise create(String name) {
        return Franchise.builder()
                .name(NameRules.normalize(name))
                .build();
    }

    public Franchise rename(String name) {
        return toBuilder()
                .name(NameRules.normalize(name))
                .build();
    }
}
