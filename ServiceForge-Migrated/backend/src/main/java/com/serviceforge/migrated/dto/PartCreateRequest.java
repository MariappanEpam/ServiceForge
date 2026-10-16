package com.serviceforge.migrated.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PartCreateRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Z0-9-]{3,32}$")
        String sku,

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
