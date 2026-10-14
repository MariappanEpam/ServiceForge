package com.serviceforge.migrated.model;

public record Technician(
        long id,
        String name,
        boolean active,
        Integer age,
        String areaServed,
        String vehicleMake,
        String vehicleModel,
        Integer vehicleYear,
        String skillsCsv
) {
}
