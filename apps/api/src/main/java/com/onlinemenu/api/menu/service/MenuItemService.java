package com.onlinemenu.api.menu.service;

import com.onlinemenu.api.media.model.StoredImage;
import com.onlinemenu.api.menu.dto.request.MenuItemRequest;
import com.onlinemenu.api.menu.dto.response.MenuItemResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Public API of the menu module. */
public interface MenuItemService {

    /** Lists items; every filter is optional ({@code null} = no filter). */
    List<MenuItemResponse> findAll(Long branchId, String category, Boolean available);

    MenuItemResponse findById(Long id);

    MenuItemResponse create(MenuItemRequest request);

    MenuItemResponse update(Long id, MenuItemRequest request);

    /** Deletes the item and its image. */
    void delete(Long id);

    /** Deletes all items of a branch and their images (called when a branch is deleted). */
    void deleteAllByBranchId(Long branchId);

    // ---------- image sub-resource ----------

    /** Uploads or replaces the item image. */
    MenuItemResponse uploadImage(Long id, MultipartFile file);

    StoredImage loadImage(Long id);

    void deleteImage(Long id);
}
