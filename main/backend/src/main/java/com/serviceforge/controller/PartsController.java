package com.serviceforge.controller;

import com.serviceforge.dto.ApiError;
import com.serviceforge.model.PartReservation;
import com.serviceforge.service.IPartsInventoryService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
public class PartsController {

    private final IPartsInventoryService inventoryService;

    public PartsController(IPartsInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<PartReservation>> listReservations() {
        return ResponseEntity.ok(inventoryService.listReservations());
    }

    @PostMapping("/{sku}/restock")
    public ResponseEntity<?> restock(@PathVariable String sku, @RequestParam @Min(1) int quantity) {
        inventoryService.restock(sku, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reserve(@RequestParam String sku, @RequestParam @Min(1) int quantity, @RequestParam Long jobId) {
        try {
            PartReservation r = inventoryService.reserve(sku, quantity, jobId);
            return ResponseEntity.status(201).body(r);
        } catch (com.serviceforge.exception.InsufficientStockException ex) {
            // Defensive: return a 400 with ApiError payload so the frontend receives
            // a structured error instead of a 500 when exception mapping is not applied.
            ApiError err = new ApiError(400, "INSUFFICIENT_STOCK", ex.getMessage(), ex.getMessage(), null);
            return ResponseEntity.status(400).body(err);
        }
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        inventoryService.releaseReservation(id);
        return ResponseEntity.noContent().build();
    }
}
