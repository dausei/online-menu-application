package com.onlinemenu.api.menu.mapper;

import com.onlinemenu.api.menu.dto.request.MenuItemRequest;
import com.onlinemenu.api.menu.dto.response.MenuItemResponse;
import com.onlinemenu.api.menu.entity.MenuItem;

/** Converts between menu item DTOs and the {@link MenuItem} entity. */
public final class MenuItemMapper {

    private MenuItemMapper() {
    }

    public static MenuItemResponse toResponse(MenuItem m) {
        // The DB stores only the storage key; the public URL is built here.
        String imageUrl = m.getImageKey() == null ? null : "/api/v1/menu-items/" + m.getId() + "/image";
        return new MenuItemResponse(m.getId(), m.getBranchId(), m.getName(), m.getDescription(),
                m.getCategory(), m.getPrice(), m.isAvailable(), m.getSortOrder(), imageUrl,
                m.getCreatedAt(), m.getUpdatedAt());
    }

    /** Copies request fields into the entity (used for both create and full update). */
    public static void updateEntity(MenuItem item, MenuItemRequest r) {
        item.setBranchId(r.branchId());
        item.setName(r.name().trim());
        item.setDescription(r.description());
        item.setCategory(r.category().trim());
        item.setPrice(r.price());
        item.setAvailable(r.available() == null || r.available());
        item.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());
    }
}
