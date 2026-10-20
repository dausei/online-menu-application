package com.onlinemenu.api.branch.controller;

import com.onlinemenu.api.branch.dto.request.BranchRequest;
import com.onlinemenu.api.branch.dto.response.BranchResponse;
import com.onlinemenu.api.branch.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Branches", description = "Restaurant branches")
@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

    private final BranchService service;

    public BranchController(BranchService service) {
        this.service = service;
    }

    @Operation(summary = "List all branches")
    @GetMapping
    public List<BranchResponse> list() {
        return service.findAll();
    }

    @Operation(summary = "Get a branch by id")
    @GetMapping("/{id}")
    public BranchResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a branch")
    @PostMapping
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody BranchRequest request) {
        BranchResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a branch (full replacement)")
    @PutMapping("/{id}")
    public BranchResponse update(@PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a branch together with its menu items")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
