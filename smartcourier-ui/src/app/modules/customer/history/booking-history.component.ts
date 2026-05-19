import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../../core/services/api.service';
import { Delivery } from '../../../core/models/delivery.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-booking-history',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatTableModule, MatPaginatorModule,
            MatSortModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, StatusBadgeComponent],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">Booking History</h2>
        <p class="page-subtitle">All your parcel bookings</p>
      </div>
      <a mat-flat-button routerLink="/customer/book" class="primary-btn" id="history-book-btn">
        <mat-icon>add</mat-icon> New Booking
      </a>
    </div>

    <mat-card class="table-card">
      <mat-card-content>
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="filter-field">
            <mat-label>Search parcels</mat-label>
            <input matInput (keyup)="applyFilter($event)" placeholder="Tracking no, status..." id="history-filter">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
        </div>

        <div class="table-container">
          <table mat-table [dataSource]="dataSource" matSort id="history-table">
            <ng-container matColumnDef="trackingNumber">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Tracking #</th>
              <td mat-cell *matCellDef="let row">
                <code class="tracking-code">{{ row.trackingNumber }}</code>
              </td>
            </ng-container>

            <ng-container matColumnDef="serviceType">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Service</th>
              <td mat-cell *matCellDef="let row">{{ row.serviceType }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
              <td mat-cell *matCellDef="let row">
                <app-status-badge [status]="row.status"></app-status-badge>
              </td>
            </ng-container>

            <ng-container matColumnDef="totalCharge">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Charge</th>
              <td mat-cell *matCellDef="let row">₹{{ row.totalCharge | number:'1.2-2' }}</td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Date</th>
              <td mat-cell *matCellDef="let row">
                <div>Created: {{ row.createdAt + 'Z' | date:'dd MMM yyyy' }}</div>
                <div class="updated-date">Updated: {{ row.updatedAt + 'Z' | date:'dd MMM yyyy, h:mm a' }}</div>
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Actions</th>
              <td mat-cell *matCellDef="let row">
                <a mat-icon-button [routerLink]="['/customer/track']" [queryParams]="{ tn: row.trackingNumber }"
                   [id]="'track-btn-' + row.id" title="Track parcel">
                  <mat-icon>radar</mat-icon>
                </a>
                <button *ngIf="row.status === 'BOOKED'" mat-icon-button class="cancel-btn" (click)="cancelDelivery(row.id)" title="Cancel Parcel">
                  <mat-icon>cancel</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;" class="table-row"></tr>

            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell no-data" [attr.colspan]="columns.length">
                <mat-icon>inbox</mat-icon>
                <span>No bookings found</span>
              </td>
            </tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[10, 20, 50]" showFirstLastButtons id="history-paginator"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .primary-btn { background: #1E3A5F; color: white; border-radius: 8px; display: flex; align-items: center; gap: 4px; }
    .table-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    mat-card-content { padding: 20px !important; }
    .table-toolbar { margin-bottom: 12px; }
    .filter-field { width: 320px; }
    .table-container { overflow-x: auto; }
    table { width: 100%; }
    th.mat-header-cell { color: #6B7280; font-weight: 600; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.5px; }
    .table-row { cursor: pointer; transition: background 0.15s; }
    .table-row:hover { background: #F9FAFB; }
    .tracking-code { font-family: monospace; font-size: 0.85rem; background: #F3F4F6; padding: 2px 6px; border-radius: 4px; }
    .no-data { text-align: center; padding: 40px; color: #9CA3AF; }
    .no-data mat-icon, .no-data span { display: block; }
    .cancel-btn { color: #DC2626; }
    .updated-date { font-size: 0.75rem; color: #6B7280; margin-top: 2px; }
  `]
})
export class BookingHistoryComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  columns = ['trackingNumber', 'serviceType', 'status', 'totalCharge', 'createdAt', 'actions'];
  dataSource = new MatTableDataSource<Delivery>();

  ngOnInit(): void {
    this.api.getMyDeliveries(0, 100).subscribe({
      next: (res) => {
        this.dataSource.data = res.data?.content ?? [];
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      }
    });
  }

  applyFilter(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.dataSource.filter = value.trim().toLowerCase();
  }

  cancelDelivery(id: number): void {
    if (confirm('Are you sure you want to cancel this parcel?')) {
      this.api.cancelDelivery(id).subscribe({
        next: () => {
          this.snackBar.open('Parcel cancelled successfully', '✓', { duration: 3000, panelClass: ['sc-snackbar-success'] });
          this.ngOnInit(); // Refresh list
        },
        error: () => {
          this.snackBar.open('Failed to cancel parcel', 'X', { duration: 3000, panelClass: ['sc-snackbar-error'] });
        }
      });
    }
  }
}
