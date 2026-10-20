package com.onlinemenu.api.branch.service.impl;

import com.onlinemenu.api.branch.dto.request.BranchRequest;
import com.onlinemenu.api.branch.entity.Branch;
import com.onlinemenu.api.branch.event.BranchDeletedEvent;
import com.onlinemenu.api.branch.repository.BranchRepository;
import com.onlinemenu.api.common.exception.ConflictException;
import com.onlinemenu.api.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests without Spring context and database. */
class BranchServiceImplTest {

    private BranchRepository repository;
    private ApplicationEventPublisher events;
    private BranchServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(BranchRepository.class);
        events = mock(ApplicationEventPublisher.class);
        service = new BranchServiceImpl(repository, events);
    }

    @Test
    void createRejectsDuplicateSlug() {
        when(repository.existsBySlug("downtown")).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new BranchRequest("Downtown", "downtown", null, null, true)))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void deletePublishesEventBeforeDeleting() {
        Branch branch = new Branch();
        when(repository.findById(1L)).thenReturn(Optional.of(branch));

        service.delete(1L);

        var order = inOrder(events, repository);
        order.verify(events).publishEvent(new BranchDeletedEvent(1L));
        order.verify(repository).delete(branch);
    }
}
