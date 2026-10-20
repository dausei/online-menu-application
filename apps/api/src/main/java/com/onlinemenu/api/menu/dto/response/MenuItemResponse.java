package com.onlinemenu.api.menu.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Menu item as returned by the API.
 *
 * @param imageUrl relative URL of the image, or {@code null} when the item has no image
 */
public record MenuItemResponse(
        Long id,
        Long branchId,
        String name,
        String description,
        String category,
        BigDecimal price,
        boolean available,
        int sortOrder,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
