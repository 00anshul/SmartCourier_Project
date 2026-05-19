import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
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

interface CustomerSummary {
  customerId: number;
  totalDeliveries: number;
  lastActivity: string;
  statuses: string[];
}

@Component({
  selector: 'app-admin-customers',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatPaginatorModule, MatSortModule,
            MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">All Customers</h2>
        <p class="page-subtitle">Customer activity derived from delivery records</p>
      </div>
    </div>

    <mat-card class="table-card">
      <mat-card-content>
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="filter-field">
            <mat-label>Search by customer ID</mat-label>
            <input matInput (keyup)="applyFilter($event)" id="customers-filter">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
          <span class="total-count">{{ dataSource.filteredData.length }} customers</span>
        </div>
        <div class="table-container">
          <table mat-table [dataSource]="dataSource" matSort id="customers-table">
            <ng-container matColumnDef="customerId">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Customer ID</th>
              <td mat-cell *matCellDef="let row"><strong>#{{ row.customerId }}</strong></td>
            </ng-container>
            <ng-container matColumnDef="totalDeliveries">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Total Deliveries</th>
              <td mat-cell *matCellDef="let row">{{ row.totalDeliveries }}</td>
            </ng-container>
            <ng-container matColumnDef="lastActivity">
              <th mat-header-cell *matHeaderCellDef mat-sort-header>Last Activity</th>
              <td mat-cell *matCellDef="let row">{{ row.lastActivity | date:'dd MMM yyyy' }}</td>
            </ng-container>
            <ng-container matColumnDef="statuses">
              <th mat-header-cell *matHeaderCellDef>Delivery Statuses</th>
              <td mat-cell *matCellDef="let row">
                <span class="status-pill" *ngFor="let s of row.statuses.slice(0,3)">{{ s }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Actions</th>
              <td mat-cell *matCellDef="let row">
                <button mat-icon-button (click)="viewDetails(row)" [id]="'view-customer-' + row.customerId">
                  <mat-icon>visibility</mat-icon>
                </button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns;" class="table-row"></tr>
            <tr class="mat-row" *matNoDataRow>
              <td class="no-data" [attr.colspan]="columns.length">No customers found</td>
            </tr>
          </table>
        </div>
        <mat-paginator [pageSizeOptions]="[10, 20, 50]" showFirstLastButtons id="customers-paginator"></mat-paginator>
      </mat-card-content>
    </mat-card>

    <mat-card class="detail-card" *ngIf="selectedCustomer">
      <mat-card-header>
        <mat-card-title>Customer #{{ selectedCustomer.customerId }} — Delivery History</mat-card-title>
        <button mat-icon-button (click)="selectedCustomer = null" id="close-detail-btn"><mat-icon>close</mat-icon></button>
      </mat-card-header>
      <mat-card-content>
        <div class="delivery-mini" *ngFor="let d of customerDeliveries">
          <code class="tracking-code">{{ d.trackingNumber }}</code>
          <span>{{ d.serviceType }}</span>
          <span>{{ d.status }}</span>
          <span>{{ d.createdAt | date:'dd MMM yyyy' }}</span>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .table-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; margin-bottom: 24px; }
    mat-card-content { padding: 20px !important; }
    .table-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
    .filter-field { flex: 1; max-width: 360px; }
    .total-count { color: #6B7280; font-size: 0.875rem; }
    .table-container { overflow-x: auto; }
    table { width: 100%; }
    th.mat-header-cell { color: #6B7280; font-weight: 600; font-size: 0.78rem; text-transform: uppercase; }
    .table-row:hover { background: #F9FAFB; cursor: pointer; }
    .status-pill { background: #F3F4F6; border-radius: 4px; padding: 2px 6px; font-size: 0.72rem; margin-right: 4px; }
    .no-data { text-align: center; padding: 40px; color: #9CA3AF; }
    .detail-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    mat-card-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px 8px; }
    .delivery-mini { display: flex; gap: 16px; align-items: center; padding: 10px 0; border-bottom: 1px solid #F3F4F6; font-size: 0.875rem; }
    .tracking-code { font-family: monospace; background: #F3F4F6; padding: 2px 6px; border-radius: 4px; }
  `]
})
export class AdminCustomersComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private api = inject(ApiService);
  columns = ['customerId', 'totalDeliveries', 'lastActivity', 'statuses', 'actions'];
  dataSource = new MatTableDataSource<CustomerSummary>();
  selectedCustomer: CustomerSummary | null = null;
  customerDeliveries: Delivery[] = [];
  private allDeliveries: Delivery[] = [];

  ngOnInit(): void {
    this.api.getAllDeliveries(0, 500).subscribe({
      next: (res) => {
        this.allDeliveries = res.data?.content ?? [];
        this.buildCustomerTable();
        setTimeout(() => {
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
      }
    });
  }

  private buildCustomerTable(): void {
    const map = new Map<number, Delivery[]>();
    for (const d of this.allDeliveries) {
      if (!map.has(d.customerId)) map.set(d.customerId, []);
      map.get(d.customerId)!.push(d);
    }
    this.dataSource.data = Array.from(map.entries()).map(([id, deliveries]) => ({
      customerId: id,
      totalDeliveries: deliveries.length,
      lastActivity: deliveries.sort((a, b) => b.createdAt.localeCompare(a.createdAt))[0]?.createdAt ?? '',
      statuses: [...new Set(deliveries.map(d => d.status))]
    }));
  }

  applyFilter(event: Event): void {
    this.dataSource.filter = (event.target as HTMLInputElement).value.trim().toLowerCase();
  }

  viewDetails(customer: CustomerSummary): void {
    this.selectedCustomer = customer;
    this.customerDeliveries = this.allDeliveries.filter(d => d.customerId === customer.customerId);
  }
}
