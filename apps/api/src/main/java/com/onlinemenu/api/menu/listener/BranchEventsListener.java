package com.onlinemenu.api.menu.listener;

import com.onlinemenu.api.branch.event.BranchDeletedEvent;
import com.onlinemenu.api.menu.service.MenuItemService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to events of the branch module.
 *
 * <p>The listener runs synchronously inside the transaction of
 * {@code BranchService.delete}, so menu items are removed before the branch row
 * (the FK {@code menu_items.branch_id} would otherwise block the delete).
 * When modules become separate services, this is the place to consume a message from a broker.
 */
@Component
public class BranchEventsListener {

    private final MenuItemService menuItems;

    public BranchEventsListener(MenuItemService menuItems) {
        this.menuItems = menuItems;
    }

    @EventListener
    public void onBranchDeleted(BranchDeletedEvent event) {
        menuItems.deleteAllByBranchId(event.branchId());
    }
}
