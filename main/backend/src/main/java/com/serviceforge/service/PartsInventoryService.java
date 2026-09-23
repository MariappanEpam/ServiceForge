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
    private final AtomicLong reservationSeq = new AtomicLong(1);

    public PartsInventoryService(MockDataStore dataStore) {
        this.dataStore = dataStore;
        // seed minimal inventory for demo
        inventory.put("PART-001", 10);
        inventory.put("PART-002", 5);
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
    public synchronized PartReservation reserve(String sku, int quantity, Long jobId) throws InsufficientStockException {
        int available = inventory.getOrDefault(sku, 0);
        if (available < quantity) {
            throw new InsufficientStockException("Insufficient stock for " + sku);
        }
        inventory.put(sku, available - quantity);
        PartReservation r = new PartReservation(reservationSeq.getAndIncrement(), sku, quantity, jobId);
        reservations.add(r);
        return r;
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
}
