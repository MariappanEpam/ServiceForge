package com.serviceforge.migrated.model;

public record Part(
        String sku,
        String name,
        String description,
        boolean active,
        boolean orderable,
        int quantityOnHand
) {
}
