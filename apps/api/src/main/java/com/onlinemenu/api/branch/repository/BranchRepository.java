package com.onlinemenu.api.branch.repository;

import com.onlinemenu.api.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for branches.
 *
 * <p>Internal to the branch module: other modules must use
 * {@link com.onlinemenu.api.branch.service.BranchService} (enforced by ArchitectureTest).
 */
public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
