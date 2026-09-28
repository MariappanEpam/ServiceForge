package com.serviceforge.model;

import java.time.Instant;

public class PartReservation {
    private final Long id;
    private final String sku;
    // Quantity reserved (keeps backward compatibility with older consumers)
    private final int quantity;
    private final Long jobId;

    // New audit fields required by the parts reservation rules
    private final Long technicianId; // optional
    private final int requestedQuantity;
    private final int shortageQuantity;
    private final String timestamp; // ISO-8601
    private final String status; // Reserved, Partially Reserved, Awaiting Stock

    // Backwards-compatible constructor: old callers that passed (id, sku, quantity, jobId)
    // will receive a reservation with requestedQuantity == quantity and status="Reserved"
    public PartReservation(Long id, String sku, int quantity, Long jobId) {
        this(id, sku, quantity, jobId, null, quantity, 0, Instant.now().toString(), "Reserved");
    }

    // Full constructor for new flows
    public PartReservation(Long id, String sku, int reservedQuantity, Long jobId, Long technicianId,
                           int requestedQuantity, int shortageQuantity, String timestamp, String status) {
        this.id = id;
        this.sku = sku;
        this.quantity = reservedQuantity;
        this.jobId = jobId;
        this.technicianId = technicianId;
        this.requestedQuantity = requestedQuantity;
        this.shortageQuantity = shortageQuantity;
        this.timestamp = timestamp;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    // Kept for compatibility: reserved quantity
    public int getQuantity() { return quantity; }
    public Long getJobId() { return jobId; }

    // New getters
    public Long getTechnicianId() { return technicianId; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public int getShortageQuantity() { return shortageQuantity; }
    public String getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
