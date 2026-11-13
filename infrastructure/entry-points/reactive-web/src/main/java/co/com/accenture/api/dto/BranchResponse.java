package co.com.accenture.api.dto;

import co.com.accenture.model.branch.Branch;

public record BranchResponse(String id, String franchiseId, String name) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getFranchiseId(), branch.getName());
    }
}
