import { Component } from '@angular/core';
import { TechnicianService } from '../services/technician.service';
import { Technician, UpdateTechnicianRequest } from '../models/technician.model';

@Component({
  selector: 'app-technician-detail',
  templateUrl: './technician-detail.component.html',
  styleUrls: ['./technician-detail.component.css']
})
export class TechnicianDetailComponent {

  technicianIdInput = '';
  technician: Technician | null = null;

  edit: UpdateTechnicianRequest = {
    name: '',
    trade: '',
    region: '',
    email: '',
    phone: ''
  };

  lastInviteToken: string | null = null;
  errorMessage = '';

  constructor(private technicianService: TechnicianService) {}

  load(): void {
    this.errorMessage = '';
    this.lastInviteToken = null;

    const id = Number(this.technicianIdInput);
    if (!id || isNaN(id)) {
      this.errorMessage = 'Enter a valid technician ID.';
      return;
    }

    this.technicianService.getTechnician(id).subscribe({
      next: (t) => {
        this.technician = t;
        this.edit = {
          name: t.name,
          trade: t.trade || '',
          region: t.region,
          email: t.email || '',
          phone: t.phone || ''
        };
      },
      error: (err) => {
        this.technician = null;
        this.errorMessage = err?.error?.message || 'Technician not found.';
      }
    });
  }

  save(): void {
    if (!this.technician) {
      return;
    }

    this.errorMessage = '';
    this.technicianService.updateTechnician(this.technician.id, {
      name: this.edit.name,
      trade: this.edit.trade,
      region: this.edit.region,
      email: this.edit.email || undefined,
      phone: this.edit.phone || undefined
    }).subscribe({
      next: (t) => {
        this.technician = t;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Could not update technician.';
      }
    });
  }

  reinvite(): void {
    if (!this.technician) {
      return;
    }

    this.errorMessage = '';
    this.lastInviteToken = null;

    this.technicianService.reinviteTechnician(this.technician.id).subscribe({
      next: (resp) => {
        this.lastInviteToken = resp.invitationToken;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Could not reinvite.';
      }
    });
  }

  suspend(): void {
    if (!this.technician) {
      return;
    }

    this.errorMessage = '';
    this.technicianService.suspendTechnician(this.technician.id).subscribe({
      next: (t) => this.technician = t,
      error: (err) => this.errorMessage = err?.error?.message || 'Could not suspend.'
    });
  }

  reactivate(): void {
    if (!this.technician) {
      return;
    }

    this.errorMessage = '';
    this.technicianService.reactivateTechnician(this.technician.id).subscribe({
      next: (t) => this.technician = t,
      error: (err) => this.errorMessage = err?.error?.message || 'Could not reactivate.'
    });
  }

  offboard(): void {
    if (!this.technician) {
      return;
    }

    this.errorMessage = '';
    this.technicianService.offboardTechnician(this.technician.id).subscribe({
      next: (t) => this.technician = t,
      error: (err) => this.errorMessage = err?.error?.message || 'Could not offboard.'
    });
  }
}
