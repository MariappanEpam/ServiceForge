import { Component, OnInit } from '@angular/core';
import { Technician } from '../models/technician.model';
import { Job, BookJobRequest } from '../models/job.model';
import { TechnicianService } from '../services/technician.service';

@Component({
  selector: 'app-technician-calendar',
  templateUrl: './technician-calendar.component.html',
  styleUrls: ['./technician-calendar.component.css']
})
export class TechnicianCalendarComponent implements OnInit {

  technicians: Technician[] = [];
  selectedTechnicianId: number | null = null;
  jobs: Job[] = [];

  newCustomerName = '';
  newStartTime = '';
  newEndTime = '';
  errorMessage = '';

  // Keep the client-side buffer in sync with backend decision (45 minutes)
  readonly TRAVEL_BUFFER_MINUTES = 45;

  constructor(private technicianService: TechnicianService) {}

  ngOnInit(): void {
    this.technicianService.getTechnicians().subscribe(technicians => {
      this.technicians = technicians;
      if (technicians.length > 0) {
        this.selectTechnician(technicians[0].id);
      }
    });
  }

  selectTechnician(technicianId: number): void {
    this.selectedTechnicianId = technicianId;
    this.errorMessage = '';
    this.loadJobs();
  }

  loadJobs(): void {
    if (this.selectedTechnicianId === null) {
      return;
    }
    this.technicianService.getJobsForTechnician(this.selectedTechnicianId)
      .subscribe(jobs => {
        this.jobs = jobs.sort((a, b) => a.startTime.localeCompare(b.startTime));
      });
  }

  bookJob(): void {
    if (this.selectedTechnicianId === null || !this.newCustomerName || !this.newStartTime || !this.newEndTime) {
      this.errorMessage = 'Fill in customer name, start time, and end time.';
      return;
    }

    // Basic client-side validation: end must be after start
    const start = new Date(this.newStartTime);
    const end = new Date(this.newEndTime);
    if (isNaN(start.getTime()) || isNaN(end.getTime()) || end.getTime() <= start.getTime()) {
      this.errorMessage = 'End time must be after start time.';
      return;
    }

    // Check against already-loaded jobs for a likely conflict using the same travel-buffer rule
    const newOccupiedStart = start.getTime();
    const newOccupiedEndWithBuffer = end.getTime() + this.TRAVEL_BUFFER_MINUTES * 60 * 1000;

    const conflict = this.jobs.some(existing => {
      const existingStart = new Date(existing.startTime).getTime();
      const existingEndWithBuffer = new Date(existing.endTime).getTime() + this.TRAVEL_BUFFER_MINUTES * 60 * 1000;
      return newOccupiedStart < existingEndWithBuffer && end.getTime() > existingStart;
    });

    if (conflict) {
      this.errorMessage = `Selected time overlaps an existing job. The system enforces a ${this.TRAVEL_BUFFER_MINUTES}-minute travel buffer — choose a different time or technician.`;
      return;
    }

    const request: BookJobRequest = {
      technicianId: this.selectedTechnicianId,
      customerName: this.newCustomerName,
      startTime: this.newStartTime,
      endTime: this.newEndTime
    };

    this.errorMessage = '';
    this.technicianService.bookJob(request).subscribe({
      next: () => {
        this.newCustomerName = '';
        this.newStartTime = '';
        this.newEndTime = '';
        this.loadJobs();
      },
      error: (err) => {
        // Prefer server-provided message for known conflicts (409). Fall back to generic message.
        if (err && err.status === 409) {
          this.errorMessage = err.error?.message || 'Technician unavailable for the requested time (conflict).';
        } else if (err && err.status === 400) {
          this.errorMessage = err.error?.message || 'Invalid request. Check times and try again.';
        } else {
          this.errorMessage = err?.error?.message || 'Could not book this job.';
        }
      }
    });
  }
}
