package co.com.accenture.api.dto;

import co.com.accenture.model.branch.Branch;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Branch of a franchise")
public record BranchResponse(
        @Schema(description = "Branch identifier", example = "6af7a0b1c2d3e4f5a6b7c8da") String id,
        @Schema(description = "Identifier of the franchise the branch belongs to", example = "6af7a0b1c2d3e4f5a6b7c8d9")
        String franchiseId,
        @Schema(description = "Branch name", example = "Airport") String name) {

    public static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getFranchiseId(), branch.getName());
    }
}
