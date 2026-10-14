package com.serviceforge.migrated.service;

import com.serviceforge.migrated.model.Part;
import com.serviceforge.migrated.repo.JdbcPartRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.serviceforge.migrated.api.ConflictException;
import com.serviceforge.migrated.api.NotFoundException;

import java.util.List;

@Service
public class PartsCatalogService {
    private final JdbcPartRepository parts;

    public PartsCatalogService(JdbcPartRepository parts) {
        this.parts = parts;
    }

    public List<Part> listCatalog() {
        return parts.listActiveOrderable();
    }

    public List<Part> list(String q, boolean activeOnly) {
        return parts.list(q, activeOnly);
    }

    public Part getBySku(String sku) {
        Part p = parts.getBySku(sku);
        if (p == null || !p.active()) {
            throw new NotFoundException("Part not found: " + sku, "PART_NOT_FOUND");
        }
        return p;
    }

    public Part create(Part part) {
        try {
            return parts.insert(part);
        } catch (DuplicateKeyException ex) {
            throw new ConflictException("Part SKU already exists: " + part.sku(), "PART_SKU_EXISTS");
        }
    }

    public Part update(String sku, Part part) {
        int updated = parts.update(sku, part);
        if (updated == 0) {
            throw new NotFoundException("Part not found: " + sku, "PART_NOT_FOUND");
        }
        return parts.getBySku(sku);
    }

    public void delete(String sku) {
        int updated = parts.softDelete(sku);
        if (updated == 0) {
            throw new NotFoundException("Part not found: " + sku, "PART_NOT_FOUND");
        }
    }
}
