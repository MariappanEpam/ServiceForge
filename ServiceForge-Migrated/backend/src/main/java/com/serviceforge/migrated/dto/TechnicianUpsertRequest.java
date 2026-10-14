package com.serviceforge.migrated.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TechnicianUpsertRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @Min(16)
        @Max(80)
        Integer age,

        @NotBlank
        @Size(max = 120)
        String areaServed,

        @Valid
        VehicleDetailsDto vehicle,

        @Size(max = 50)
        List<@Size(max = 50) String> skills
) {
}
