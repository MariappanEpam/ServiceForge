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

    @GetMapping("/orders")
    public ResponseEntity<?> listOrders() {
        // inventoryService may expose listOrders directly; use reflection-free access via cast
        if (inventoryService instanceof com.serviceforge.service.PartsInventoryService) {
            return ResponseEntity.ok(((com.serviceforge.service.PartsInventoryService) inventoryService).listOrders());
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/{sku}/restock")
    public ResponseEntity<?> restock(@PathVariable String sku, @RequestParam @Min(1) int quantity) {
        inventoryService.restock(sku, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reserve(@RequestParam String sku, @RequestParam @Min(1) int quantity, @RequestParam Long jobId, @RequestParam(required = false) Long technicianId) {
        PartReservation r = inventoryService.reserve(sku, quantity, jobId, technicianId);
        return ResponseEntity.status(r.getStatus().equals("Reserved") ? 201 : 200).body(r);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        inventoryService.releaseReservation(id);
        return ResponseEntity.noContent().build();
    }
}
