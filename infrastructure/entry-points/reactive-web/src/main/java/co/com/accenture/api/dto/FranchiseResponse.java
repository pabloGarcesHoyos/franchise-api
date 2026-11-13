package co.com.accenture.api.dto;

import co.com.accenture.model.franchise.Franchise;

public record FranchiseResponse(String id, String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(franchise.getId(), franchise.getName());
    }
}
