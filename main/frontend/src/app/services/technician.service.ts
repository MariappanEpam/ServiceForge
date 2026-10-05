import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ActivateTechnicianRequest,
  CreateTechnicianRequest,
  Technician,
  TechnicianInviteResponse,
  UpdateTechnicianRequest
} from '../models/technician.model';
import { Job, BookJobRequest } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class TechnicianService {

  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getTechnicians(params?: { status?: string; search?: string }): Observable<Technician[]> {
    const query: string[] = [];
    if (params?.status) {
      query.push(`status=${encodeURIComponent(params.status)}`);
    }
    if (params?.search) {
      query.push(`search=${encodeURIComponent(params.search)}`);
    }
    const qs = query.length ? `?${query.join('&')}` : '';
    return this.http.get<Technician[]>(`${this.baseUrl}/technicians${qs}`);
  }

  getTechnician(id: number): Observable<Technician> {
    return this.http.get<Technician>(`${this.baseUrl}/technicians/${id}`);
  }

  createTechnician(request: CreateTechnicianRequest, role: 'admin' | 'dispatcher' = 'admin'): Observable<TechnicianInviteResponse> {
    return this.http.post<TechnicianInviteResponse>(`${this.baseUrl}/technicians`, request, {
      headers: { 'X-SF-Role': role }
    });
  }

  updateTechnician(id: number, request: UpdateTechnicianRequest, role: 'admin' | 'dispatcher' = 'admin'): Observable<Technician> {
    return this.http.put<Technician>(`${this.baseUrl}/technicians/${id}`, request, {
      headers: { 'X-SF-Role': role }
    });
  }

  reinviteTechnician(id: number, role: 'admin' | 'dispatcher' = 'admin'): Observable<TechnicianInviteResponse> {
    return this.http.post<TechnicianInviteResponse>(`${this.baseUrl}/technicians/${id}/reinvite`, {}, {
      headers: { 'X-SF-Role': role }
    });
  }

  activateTechnician(request: ActivateTechnicianRequest): Observable<Technician> {
    return this.http.post<Technician>(`${this.baseUrl}/technicians/activate`, request);
  }

  suspendTechnician(id: number, role: 'admin' | 'dispatcher' = 'admin'): Observable<Technician> {
    return this.http.post<Technician>(`${this.baseUrl}/technicians/${id}/suspend`, {}, {
      headers: { 'X-SF-Role': role }
    });
  }

  reactivateTechnician(id: number, role: 'admin' | 'dispatcher' = 'admin'): Observable<Technician> {
    return this.http.post<Technician>(`${this.baseUrl}/technicians/${id}/reactivate`, {}, {
      headers: { 'X-SF-Role': role }
    });
  }

  offboardTechnician(id: number, role: 'admin' | 'dispatcher' = 'admin'): Observable<Technician> {
    return this.http.post<Technician>(`${this.baseUrl}/technicians/${id}/offboard`, {}, {
      headers: { 'X-SF-Role': role }
    });
  }

  getJobsForTechnician(technicianId: number): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.baseUrl}/technicians/${technicianId}/jobs`);
  }

  bookJob(request: BookJobRequest): Observable<Job> {
    return this.http.post<Job>(`${this.baseUrl}/jobs`, request);
  }
}
