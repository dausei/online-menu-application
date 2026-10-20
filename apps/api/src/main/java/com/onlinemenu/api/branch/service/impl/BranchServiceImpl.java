package com.onlinemenu.api.branch.service.impl;

import com.onlinemenu.api.branch.dto.request.BranchRequest;
import com.onlinemenu.api.branch.dto.response.BranchResponse;
import com.onlinemenu.api.branch.entity.Branch;
import com.onlinemenu.api.branch.event.BranchDeletedEvent;
import com.onlinemenu.api.branch.mapper.BranchMapper;
import com.onlinemenu.api.branch.repository.BranchRepository;
import com.onlinemenu.api.branch.service.BranchService;
import com.onlinemenu.api.common.exception.ConflictException;
import com.onlinemenu.api.common.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService {

    private final BranchRepository repository;
    private final ApplicationEventPublisher events;

    public BranchServiceImpl(BranchRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Override
    public List<BranchResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(BranchMapper::toResponse)
                .toList();
    }

    @Override
    public BranchResponse findById(Long id) {
        return BranchMapper.toResponse(getOrThrow(id));
    }

    @Override
    public boolean exists(Long id) {
        return id != null && repository.existsById(id);
    }

    @Override
    @Transactional
    public BranchResponse create(BranchRequest request) {
        if (repository.existsBySlug(request.slug())) {
            throw new ConflictException("Branch with slug '" + request.slug() + "' already exists");
        }
        Branch branch = new Branch();
        BranchMapper.updateEntity(branch, request);
        return BranchMapper.toResponse(repository.saveAndFlush(branch));
    }

    @Override
    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = getOrThrow(id);
        if (repository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ConflictException("Branch with slug '" + request.slug() + "' already exists");
        }
        BranchMapper.updateEntity(branch, request);
        return BranchMapper.toResponse(repository.saveAndFlush(branch));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Branch branch = getOrThrow(id);
        // Let other modules clean up their data (menu items, images) in the same transaction.
        events.publishEvent(new BranchDeletedEvent(id));
        repository.delete(branch);
    }

    private Branch getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Branch", id));
    }
}
