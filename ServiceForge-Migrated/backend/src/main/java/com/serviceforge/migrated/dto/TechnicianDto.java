package com.serviceforge.migrated.dto;

import java.util.List;

public record TechnicianDto(
        long id,
        String name,
        boolean active,
        Integer age,
        String areaServed,
        VehicleDetailsDto vehicle,
        List<String> skills
) {
}
