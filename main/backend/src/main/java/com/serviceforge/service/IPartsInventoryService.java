package com.serviceforge.service;

import com.serviceforge.exception.InsufficientStockException;
import com.serviceforge.exception.ReservationNotFoundException;
import com.serviceforge.model.PartReservation;

import java.util.List;

public interface IPartsInventoryService {
    int getQuantity(String sku);

    void restock(String sku, int quantity);

    /**
     * Reserve parts from inventory.
     * @param sku part SKU
     * @param quantity requested quantity
     * @param jobId job the reservation is for
     * @param technicianId optional technician creating the reservation
     * @return PartReservation audit record (may be full, partial, or awaiting stock)
     */
    PartReservation reserve(String sku, int quantity, Long jobId, Long technicianId) throws InsufficientStockException;

    void releaseReservation(Long reservationId) throws ReservationNotFoundException;

    List<PartReservation> listReservations();
}
