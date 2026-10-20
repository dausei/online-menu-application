package com.onlinemenu.api.branch.service;

import com.onlinemenu.api.branch.dto.request.BranchRequest;
import com.onlinemenu.api.branch.dto.response.BranchResponse;

import java.util.List;

/** Public API of the branch module. Other modules depend on this interface only. */
public interface BranchService {

    List<BranchResponse> findAll();

    BranchResponse findById(Long id);

    /** Used by other modules to validate references to a branch. */
    boolean exists(Long id);

    BranchResponse create(BranchRequest request);

    BranchResponse update(Long id, BranchRequest request);

    /** Deletes the branch; publishes {@link com.onlinemenu.api.branch.event.BranchDeletedEvent} first. */
    void delete(Long id);
}
