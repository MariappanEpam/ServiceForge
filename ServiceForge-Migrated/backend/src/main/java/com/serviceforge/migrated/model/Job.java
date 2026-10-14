package com.serviceforge.migrated.model;

import java.time.Instant;

public record Job(
        long id,
        long technicianId,
        String customerName,
        Instant startTime,
        Instant endTime
) {
}
