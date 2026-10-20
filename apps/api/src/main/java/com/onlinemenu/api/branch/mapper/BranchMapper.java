package com.onlinemenu.api.branch.mapper;

import com.onlinemenu.api.branch.dto.request.BranchRequest;
import com.onlinemenu.api.branch.dto.response.BranchResponse;
import com.onlinemenu.api.branch.entity.Branch;

/** Converts between branch DTOs and the {@link Branch} entity. */
public final class BranchMapper {

    private BranchMapper() {
    }

    public static BranchResponse toResponse(Branch b) {
        return new BranchResponse(b.getId(), b.getName(), b.getSlug(), b.getAddress(),
                b.getPhone(), b.isActive(), b.getCreatedAt(), b.getUpdatedAt());
    }

    /** Copies request fields into the entity (used for both create and full update). */
    public static void updateEntity(Branch branch, BranchRequest r) {
        branch.setName(r.name().trim());
        branch.setSlug(r.slug());
        branch.setAddress(r.address());
        branch.setPhone(r.phone());
        branch.setActive(r.active() == null || r.active());
    }
}
