import { Component, OnInit } from '@angular/core';
import { TechnicianService } from '../services/technician.service';
import { CreateTechnicianRequest, Technician, TechnicianInviteResponse } from '../models/technician.model';

@Component({
  selector: 'app-technician-management',
  templateUrl: './technician-management.component.html',
  styleUrls: ['./technician-management.component.css']
})
export class TechnicianManagementComponent implements OnInit {

  technicians: Technician[] = [];
  errorMessage = '';

  filterStatus = '';
  search = '';

  create: CreateTechnicianRequest = {
    name: '',
    trade: '',
    region: '',
    email: '',
    phone: ''
  };

  lastInviteToken: string | null = null;

  constructor(private technicianService: TechnicianService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage = '';
    this.technicianService.getTechnicians({
      status: this.filterStatus || undefined,
      search: this.search || undefined
    }).subscribe({
      next: (techs) => {
        this.technicians = techs;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Could not load technicians.';
      }
    });
  }

  createTechnician(): void {
    this.errorMessage = '';
    this.lastInviteToken = null;

    if (!this.create.name || !this.create.trade || !this.create.region) {
      this.errorMessage = 'Name, trade, and region are required.';
      return;
    }

    this.technicianService.createTechnician({
      name: this.create.name,
      trade: this.create.trade,
      region: this.create.region,
      email: this.create.email || undefined,
      phone: this.create.phone || undefined
    }).subscribe({
      next: (resp: TechnicianInviteResponse) => {
        this.lastInviteToken = resp.invitationToken;
        this.create = { name: '', trade: '', region: '', email: '', phone: '' };
        this.load();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Could not create technician.';
      }
    });
  }
}
