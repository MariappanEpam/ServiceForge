package com.serviceforge.migrated.dto;

public record PartDto(
        String sku,
        String name,
        String description,
        int quantityOnHand,
        boolean active,
        boolean orderable
) {
}
