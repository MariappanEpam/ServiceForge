package com.serviceforge.migrated.dto;

public record JobDto(
        long id,
        long technicianId,
        String customerName,
        String startTime,
        String endTime
) {
}
