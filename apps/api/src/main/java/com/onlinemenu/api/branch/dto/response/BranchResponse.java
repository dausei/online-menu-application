package com.onlinemenu.api.branch.dto.response;

import java.time.Instant;

/** Branch as returned by the API. */
public record BranchResponse(
        Long id,
        String name,
        String slug,
        String address,
        String phone,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
