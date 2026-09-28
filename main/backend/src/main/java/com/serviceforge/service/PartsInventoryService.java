package com.serviceforge.service;

import com.serviceforge.data.MockDataStore;
import com.serviceforge.exception.InsufficientStockException;
import com.serviceforge.exception.ReservationNotFoundException;
import com.serviceforge.model.PartReservation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PartsInventoryService implements IPartsInventoryService {

    private final MockDataStore dataStore;
    private final ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();
    private final List<PartReservation> reservations = new ArrayList<>();
    private final List<com.serviceforge.model.OrderRequest> orders = new ArrayList<>();
    private final AtomicLong reservationSeq = new AtomicLong(1);
    private final AtomicLong orderSeq = new AtomicLong(1);

    public PartsInventoryService(MockDataStore dataStore) {
        this.dataStore = dataStore;
        // seed minimal inventory for demo
        inventory.put("PART-001", 10);
        inventory.put("PART-002", 5);
        // Add a demo SKU the frontend tests use so reserve requests succeed during dev
        inventory.put("SKU-1000", 3);
    }

    @Override
    public int getQuantity(String sku) {
        return inventory.getOrDefault(sku, 0);
    }

    @Override
    public synchronized void restock(String sku, int quantity) {
        inventory.merge(sku, quantity, Integer::sum);
    }

    @Override
    public synchronized PartReservation reserve(String sku, int requestedQuantity, Long jobId, Long technicianId) throws InsufficientStockException {
        int available = inventory.getOrDefault(sku, 0);
        if (available == 0) {
            // No inventory available — create order request (not implemented) and return Awaiting Stock
            PartReservation awaiting = new PartReservation(reservationSeq.getAndIncrement(), sku, 0, jobId, technicianId, requestedQuantity, requestedQuantity, java.time.Instant.now().toString(), "Awaiting Stock");
            reservations.add(awaiting);
            // create order request for full shortage
            com.serviceforge.model.OrderRequest order = new com.serviceforge.model.OrderRequest(orderSeq.getAndIncrement(), sku, requestedQuantity, awaiting.getId(), "CREATED");
            orders.add(order);
            return awaiting;
        }

        if (available >= requestedQuantity) {
            // Full reservation
            inventory.put(sku, available - requestedQuantity);
            PartReservation r = new PartReservation(reservationSeq.getAndIncrement(), sku, requestedQuantity, jobId, technicianId, requestedQuantity, 0, java.time.Instant.now().toString(), "Reserved");
            reservations.add(r);
            return r;
        }

        // Partial reservation: reserve what is available and record shortage
        int reserved = available;
        int shortage = requestedQuantity - available;
        inventory.put(sku, 0);
        // Create partial reservation and an order request for shortage (order creation not implemented)
        PartReservation partial = new PartReservation(reservationSeq.getAndIncrement(), sku, reserved, jobId, technicianId, requestedQuantity, shortage, java.time.Instant.now().toString(), "Partially Reserved");
        reservations.add(partial);
        com.serviceforge.model.OrderRequest order = new com.serviceforge.model.OrderRequest(orderSeq.getAndIncrement(), sku, shortage, partial.getId(), "CREATED");
        orders.add(order);
        return partial;
    }

    @Override
    public synchronized void releaseReservation(Long reservationId) throws ReservationNotFoundException {
        PartReservation found = reservations.stream().filter(r -> r.getId().equals(reservationId)).findFirst().orElse(null);
        if (found == null) throw new ReservationNotFoundException("No reservation " + reservationId);
        inventory.merge(found.getSku(), found.getQuantity(), Integer::sum);
        reservations.remove(found);
    }

    @Override
    public List<PartReservation> listReservations() {
        return List.copyOf(reservations);
    }

    public List<com.serviceforge.model.OrderRequest> listOrders() {
        return List.copyOf(orders);
    }
}
