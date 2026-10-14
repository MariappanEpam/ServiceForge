package com.serviceforge.migrated.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateJobRequest(
        @NotNull Long technicianId,
        @NotBlank String customerName,
        @NotBlank String startTime,
        @NotBlank String endTime
) {
}
