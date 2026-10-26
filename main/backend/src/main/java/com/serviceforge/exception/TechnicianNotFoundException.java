package com.serviceforge.exception;

/** ETSTCC-13074: no technician exists for the given id (maps to 404 TECHNICIAN_NOT_FOUND). */
public class TechnicianNotFoundException extends RuntimeException {

    public TechnicianNotFoundException(long technicianId) {
        super("Technician not found: " + technicianId);
    }
}
