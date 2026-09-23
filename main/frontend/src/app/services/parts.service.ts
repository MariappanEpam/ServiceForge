import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PartReservation {
  id: number;
  sku: string;
  quantity: number;
  jobId: number;
}

@Injectable({ providedIn: 'root' })
export class PartsService {
  private base = '/api/parts';

  constructor(private http: HttpClient) {}

  listReservations(): Observable<PartReservation[]> {
    return this.http.get<PartReservation[]>(this.base);
  }

  reserve(sku: string, quantity: number, jobId: number) {
    const params = { sku, quantity: String(quantity), jobId: String(jobId) };
    return this.http.post<PartReservation>(`${this.base}/reservations`, null, { params });
  }

  restock(sku: string, quantity: number) {
    const params = { quantity: String(quantity) };
    return this.http.post(`${this.base}/${encodeURIComponent(sku)}/restock`, null, { params });
  }

  cancelReservation(id: number) {
    return this.http.delete(`${this.base}/reservations/${id}`);
  }
}
