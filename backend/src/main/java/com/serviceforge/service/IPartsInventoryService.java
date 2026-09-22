package com.serviceforge.service;

import com.serviceforge.exception.InsufficientStockException;
import com.serviceforge.exception.ReservationNotFoundException;
import com.serviceforge.model.PartReservation;

import java.util.List;

public interface IPartsInventoryService {
    int getQuantity(String sku);

    void restock(String sku, int quantity);

    PartReservation reserve(String sku, int quantity, Long jobId) throws InsufficientStockException;

    void releaseReservation(Long reservationId) throws ReservationNotFoundException;

    List<PartReservation> listReservations();
}
