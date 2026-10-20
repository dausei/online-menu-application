package com.onlinemenu.api.menu.repository;

import com.onlinemenu.api.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data access for menu items.
 *
 * <p>Internal to the menu module: other modules must use
 * {@link com.onlinemenu.api.menu.service.MenuItemService} (enforced by ArchitectureTest).
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {

    List<MenuItem> findAllByBranchId(Long branchId);
}
