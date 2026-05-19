import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/services/api.service';
import { Delivery, DeliveryStatus } from '../../../core/models/delivery.model';
import { Hub } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-parcels',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatCardModule, MatTableModule,
            MatPaginatorModule, MatSortModule, MatButtonModule, MatIconModule,
            MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">All Parcels</h2>
        <p class="page-subtitle">Manage and update delivery statuses</p>
      </div>
    </div>

    <mat-card class="table-card">
      <mat-card-content>
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="filter-field">
            <mat-label>Search parcels</mat-label>
            <input matInput (keyup)="applyFilter($event)" placeholder="Tracking #, status, ID..." id="parcels-filter">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
          <span class="total-count">{{ dataSource.filteredData.length }} parcels</span>
        </div>

        <div class="table-container">
          <table mat-table [dataSource]="dataSource" matSort id="admin-parcels-table">

            <ng-container matColumnDef="id">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
              <td mat-cell *matCellDef="let row">{{ row.id }}</td>
            </ng-container>

            <ng-container matColumnDef="trackingNumber">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Tracking #</th>
              <td mat-cell *matCellDef="let row"><code class="tracking-code">{{ row.trackingNumber }}</code></td>
            </ng-container>

            <ng-container matColumnDef="customerId">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Customer ID</th>
              <td mat-cell *matCellDef="let row">{{ row.customerId }}</td>
            </ng-container>

            <ng-container matColumnDef="serviceType">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Service</th>
              <td mat-cell *matCellDef="let row">{{ row.serviceType }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Status & Location</th>
              <td mat-cell *matCellDef="let row">
                <div class="update-controls">
                  <mat-select [(ngModel)]="row.status"
                              class="status-select"
                              [id]="'status-select-' + row.id">
                    <mat-option *ngFor="let s of statuses" [value]="s">{{ s }}</mat-option>
                  </mat-select>
                  <mat-select [(ngModel)]="selectedHubs[row.id]"
                              placeholder="Hub (Optional)"
                              class="status-select"
                              [id]="'hub-select-' + row.id">
                    <mat-option [value]="undefined">None</mat-option>
                    <mat-option *ngFor="let h of hubs" [value]="h.id">{{ h.name }}</mat-option>
                  </mat-select>
                  <button mat-icon-button class="save-btn" (click)="onStatusChange(row)" title="Update Status">
                    <mat-icon>save</mat-icon>
                  </button>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="totalCharge">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Charge</th>
              <td mat-cell *matCellDef="let row">₹{{ row.totalCharge | number:'1.2-2' }}</td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Date</th>
              <td mat-cell *matCellDef="let row">
                <div>Created: {{ row.createdAt + 'Z' | date:'dd MMM yyyy, h:mm a' }}</div>
                <div class="updated-date">Updated: {{ row.updatedAt + 'Z' | date:'dd MMM yyyy, h:mm a' }}</div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;" class="table-row"></tr>
            <tr class="mat-row" *matNoDataRow>
              <td class="no-data" [attr.colspan]="columns.length">No parcels found</td>
            </tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[10, 20, 50]" showFirstLastButtons id="parcels-paginator"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .table-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    mat-card-content { padding: 20px !important; }
    .table-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
    .filter-field { flex: 1; max-width: 360px; }
    .total-count { color: #6B7280; font-size: 0.875rem; }
    .table-container { overflow-x: auto; }
    table { width: 100%; }
    th.mat-header-cell { color: #6B7280; font-weight: 600; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.5px; }
    .table-row:hover { background: #F9FAFB; }
    .tracking-code { font-family: monospace; font-size: 0.82rem; background: #F3F4F6; padding: 2px 6px; border-radius: 4px; }
    .update-controls { display: flex; align-items: center; gap: 8px; }
    .status-select { font-size: 0.82rem; width: 130px; }
    .save-btn { color: #1E3A5F; transform: scale(0.85); }
    .no-data { text-align: center; padding: 40px; color: #9CA3AF; }
    .updated-date { font-size: 0.75rem; color: #6B7280; margin-top: 2px; }
  `]
})
export class AdminParcelsComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  columns = ['id', 'trackingNumber', 'customerId', 'serviceType', 'status', 'totalCharge', 'createdAt'];
  dataSource = new MatTableDataSource<Delivery>();
  statuses: DeliveryStatus[] = ['BOOKED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'DELAYED', 'FAILED', 'RETURNED', 'CANCELLED'];
  hubs: Hub[] = [];
  selectedHubs: { [key: number]: number | undefined } = {};

  ngOnInit(): void {
    this.api.getAllHubs().subscribe(res => this.hubs = res.data ?? []);
    this.api.getAllDeliveries(0, 200).subscribe({
      next: (res) => {
        this.dataSource.data = res.data?.content ?? [];
        setTimeout(() => {
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
      }
    });
  }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }

  onStatusChange(delivery: Delivery): void {
    const hubId = this.selectedHubs[delivery.id];
    const hub = this.hubs.find(h => h.id === hubId);
    const location = hub ? hub.name : undefined;

    this.api.updateDeliveryStatus(delivery.id, delivery.status, hubId, location).subscribe({
      next: () => { this.snackBar.open(`Status updated to ${delivery.status}`, '✓', { duration: 2000, panelClass: ['sc-snackbar-success'] }); },
      error: () => { this.snackBar.open('Failed to update status', '✕', { duration: 3000 }); }
    });
  }
}
