package com.serviceforge.migrated.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartUpdateRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        @NotNull
        @Min(0)
        Integer quantityOnHand,

        @NotNull
        Boolean active,

        @NotNull
        Boolean orderable
) {
}
