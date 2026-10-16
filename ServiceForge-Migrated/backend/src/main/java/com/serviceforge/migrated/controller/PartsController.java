package com.serviceforge.migrated.controller;

import com.serviceforge.migrated.dto.PartCatalogItemDto;
import com.serviceforge.migrated.dto.PartCreateRequest;
import com.serviceforge.migrated.dto.PartDto;
import com.serviceforge.migrated.dto.PartUpdateRequest;
import com.serviceforge.migrated.model.Part;
import com.serviceforge.migrated.service.PartsCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/parts")
public class PartsController {
    private final PartsCatalogService catalog;

    public PartsController(PartsCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @PreAuthorize("!@securityToggle.enabled || hasAnyAuthority('ROLE_Dispatcher','ROLE_Technician')")
    public List<PartCatalogItemDto> list() {
        return catalog.listCatalog().stream()
                .map(this::toDto)
                .toList();
    }

    // Feature 7: management endpoints

    @GetMapping(params = {"activeOnly"})
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<PartDto> listManaged(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        return catalog.list(q, activeOnly).stream().map(this::toManagedDto).toList();
    }

    @GetMapping(params = {"q"})
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public List<PartDto> listManagedWithQuery(@RequestParam String q) {
        return catalog.list(q, true).stream().map(this::toManagedDto).toList();
    }

    @GetMapping("/{sku}")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public PartDto getBySku(@PathVariable String sku) {
        return toManagedDto(catalog.getBySku(sku));
    }

    @PostMapping
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public ResponseEntity<PartDto> create(@Valid @RequestBody PartCreateRequest req) {
        Part created = catalog.create(new Part(
                req.sku(),
                req.name(),
                req.description(),
                req.active(),
                req.orderable(),
                req.quantityOnHand()
        ));
        return ResponseEntity.created(URI.create("/api/parts/" + created.sku())).body(toManagedDto(created));
    }

    @PutMapping("/{sku}")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public PartDto update(@PathVariable String sku, @Valid @RequestBody PartUpdateRequest req) {
        Part updated = catalog.update(sku, new Part(
                sku,
                req.name(),
                req.description(),
                req.active(),
                req.orderable(),
                req.quantityOnHand()
        ));
        return toManagedDto(updated);
    }

    @DeleteMapping("/{sku}")
    @PreAuthorize("!@securityToggle.enabled || hasAuthority('ROLE_Dispatcher')")
    public ResponseEntity<Void> delete(@PathVariable String sku) {
        catalog.delete(sku);
        return ResponseEntity.noContent().build();
    }

    private PartCatalogItemDto toDto(Part p) {
        return new PartCatalogItemDto(p.sku(), p.name(), p.description());
    }

    private PartDto toManagedDto(Part p) {
        return new PartDto(p.sku(), p.name(), p.description(), p.quantityOnHand(), p.active(), p.orderable());
    }
}
