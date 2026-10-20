package com.onlinemenu.api.menu.controller;

import com.onlinemenu.api.media.model.StoredImage;
import com.onlinemenu.api.menu.dto.request.MenuItemRequest;
import com.onlinemenu.api.menu.dto.response.MenuItemResponse;
import com.onlinemenu.api.menu.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Tag(name = "Menu items", description = "Dishes of a branch menu and their images")
@RestController
@RequestMapping("/api/v1/menu-items")
public class MenuItemController {

    private final MenuItemService service;

    public MenuItemController(MenuItemService service) {
        this.service = service;
    }

    @Operation(summary = "List menu items (optionally filtered by branch, category, availability)")
    @GetMapping
    public List<MenuItemResponse> list(@RequestParam(required = false) Long branchId,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) Boolean available) {
        return service.findAll(branchId, category, available);
    }

    @Operation(summary = "Get a menu item by id")
    @GetMapping("/{id}")
    public MenuItemResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a menu item")
    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        MenuItemResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a menu item (full replacement)")
    @PutMapping("/{id}")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a menu item (and its image)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- image sub-resource ----------

    @Operation(summary = "Upload or replace the image of a menu item (jpeg, png, webp; max 5 MB)")
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MenuItemResponse uploadImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return service.uploadImage(id, file);
    }

    @Operation(summary = "Download the image of a menu item")
    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        StoredImage image = service.loadImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .body(image.resource());
    }

    @Operation(summary = "Delete the image of a menu item")
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        service.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
