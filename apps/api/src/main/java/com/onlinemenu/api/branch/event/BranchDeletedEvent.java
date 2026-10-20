package com.onlinemenu.api.branch.event;

/**
 * Published right before a branch is deleted.
 * Other modules (e.g. menu) react to it instead of the branch module
 * knowing about them — this keeps the dependency one-directional.
 */
public record BranchDeletedEvent(Long branchId) {
}
