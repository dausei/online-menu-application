package com.onlinemenu.api.menu.service.impl;

import com.onlinemenu.api.branch.service.BranchService;
import com.onlinemenu.api.common.exception.BadRequestException;
import com.onlinemenu.api.common.exception.NotFoundException;
import com.onlinemenu.api.media.model.StoredImage;
import com.onlinemenu.api.media.service.ImageService;
import com.onlinemenu.api.menu.dto.request.MenuItemRequest;
import com.onlinemenu.api.menu.dto.response.MenuItemResponse;
import com.onlinemenu.api.menu.entity.MenuItem;
import com.onlinemenu.api.menu.mapper.MenuItemMapper;
import com.onlinemenu.api.menu.repository.MenuItemRepository;
import com.onlinemenu.api.menu.service.MenuItemService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MenuItemServiceImpl implements MenuItemService {

    private static final String IMAGE_FOLDER = "menu-items";
    private static final Sort DEFAULT_SORT = Sort.by("category", "sortOrder", "id");

    private final MenuItemRepository repository;
    private final BranchService branches;
    private final ImageService images;

    public MenuItemServiceImpl(MenuItemRepository repository, BranchService branches, ImageService images) {
        this.repository = repository;
        this.branches = branches;
        this.images = images;
    }

    @Override
    public List<MenuItemResponse> findAll(Long branchId, String category, Boolean available) {
        Specification<MenuItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (branchId != null) {
                predicates.add(cb.equal(root.get("branchId"), branchId));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("available"), available));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, DEFAULT_SORT).stream()
                .map(MenuItemMapper::toResponse)
                .toList();
    }

    @Override
    public MenuItemResponse findById(Long id) {
        return MenuItemMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        requireBranch(request.branchId());
        MenuItem item = new MenuItem();
        MenuItemMapper.updateEntity(item, request);
        return MenuItemMapper.toResponse(repository.saveAndFlush(item));
    }

    @Override
    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = getOrThrow(id);
        requireBranch(request.branchId());
        MenuItemMapper.updateEntity(item, request);
        return MenuItemMapper.toResponse(repository.saveAndFlush(item));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MenuItem item = getOrThrow(id);
        repository.delete(item);
        images.delete(item.getImageKey());
    }

    @Override
    @Transactional
    public void deleteAllByBranchId(Long branchId) {
        List<MenuItem> items = repository.findAllByBranchId(branchId);
        repository.deleteAll(items);
        repository.flush();
        items.forEach(i -> images.delete(i.getImageKey()));
    }

    // ---------- images ----------

    @Override
    @Transactional
    public MenuItemResponse uploadImage(Long id, MultipartFile file) {
        MenuItem item = getOrThrow(id);
        String oldKey = item.getImageKey();
        item.setImageKey(images.upload(IMAGE_FOLDER, file));
        MenuItemResponse response = MenuItemMapper.toResponse(repository.saveAndFlush(item));
        images.delete(oldKey);
        return response;
    }

    @Override
    public StoredImage loadImage(Long id) {
        MenuItem item = getOrThrow(id);
        if (item.getImageKey() == null) {
            throw new NotFoundException("Image of menu item", id);
        }
        return images.load(item.getImageKey());
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        MenuItem item = getOrThrow(id);
        String key = item.getImageKey();
        if (key == null) {
            throw new NotFoundException("Image of menu item", id);
        }
        item.setImageKey(null);
        repository.saveAndFlush(item);
        images.delete(key);
    }

    // ---------- helpers ----------

    private MenuItem getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Menu item", id));
    }

    private void requireBranch(Long branchId) {
        if (!branches.exists(branchId)) {
            throw new BadRequestException("Branch with id " + branchId + " does not exist");
        }
    }
}
