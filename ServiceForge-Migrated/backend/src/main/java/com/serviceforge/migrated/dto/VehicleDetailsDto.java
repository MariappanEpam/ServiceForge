package com.serviceforge.migrated.dto;

import jakarta.validation.constraints.Size;

public record VehicleDetailsDto(
        @Size(max = 50) String make,
        @Size(max = 50) String model,
        Integer year
) {
}
