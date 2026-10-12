package com.serviceforge.model;

import java.time.Instant;

public class OrderRequest {
    private final Long id;
    private final String sku;
    private final int quantity;
    private final Long reservationId;
    private final String timestamp;
    private final String status; // e.g., CREATED, SENT, RECEIVED

    public OrderRequest(Long id, String sku, int quantity, Long reservationId, String status) {
        this.id = id;
        this.sku = sku;
        this.quantity = quantity;
        this.reservationId = reservationId;
        this.timestamp = Instant.now().toString();
        this.status = status;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public Long getReservationId() { return reservationId; }
    public String getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}
