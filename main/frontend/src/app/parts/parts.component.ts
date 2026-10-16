import { Component, OnInit } from '@angular/core';
import { PartsService, PartReservation } from '../services/parts.service';

@Component({
  selector: 'app-parts',
  templateUrl: './parts.component.html',
  styleUrls: ['./parts.component.css']
})
export class PartsComponent implements OnInit {
  reservations: PartReservation[] = [];
  sku = 'SKU-1000';
  quantity = 1;
  jobId = 1;

  constructor(private parts: PartsService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.parts.listReservations().subscribe(r => this.reservations = r);
  }

  reserve() {
    this.parts.reserve(this.sku, this.quantity, this.jobId).subscribe(() => this.load());
  }

  cancel(id: number) {
    this.parts.cancelReservation(id).subscribe(() => this.load());
  }
}
