package com.serviceforge.service;

import com.serviceforge.model.Job;

import java.time.LocalDateTime;

public interface IReservationService {
    Job bookJob(Long technicianId, String customerName, LocalDateTime startTime, LocalDateTime endTime);
}
