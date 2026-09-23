package com.serviceforge.model;

public class PartReservation {
    private final Long id;
    private final String sku;
    private final int quantity;
    private final Long jobId;

    public PartReservation(Long id, String sku, int quantity, Long jobId) {
        this.id = id;
        this.sku = sku;
        this.quantity = quantity;
        this.jobId = jobId;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public Long getJobId() { return jobId; }
}
