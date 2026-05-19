import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { DashboardData } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, RouterLink],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">Admin Dashboard</h2>
        <p class="page-subtitle">System-wide overview</p>
      </div>
    </div>

    <div class="stats-grid">
      <mat-card class="stat-card" *ngFor="let stat of stats" [id]="'stat-' + stat.key">
        <mat-card-content>
          <div class="stat-icon" [style.background]="stat.bg">
            <mat-icon [style.color]="stat.color">{{ stat.icon }}</mat-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value ?? '—' }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </mat-card-content>
      </mat-card>
    </div>

    <div class="quick-links">
      <h3 class="section-title">Quick Actions</h3>
      <div class="actions-row">
        <a mat-flat-button routerLink="/admin/parcels" id="quick-parcels-link" class="action-btn">
          <mat-icon>inventory_2</mat-icon> Manage Parcels
        </a>
        <a mat-flat-button routerLink="/admin/customers" id="quick-customers-link" class="action-btn outline">
          <mat-icon>group</mat-icon> View Customers
        </a>
        <a mat-flat-button routerLink="/admin/hubs" id="quick-hubs-link" class="action-btn outline">
          <mat-icon>warehouse</mat-icon> Manage Hubs
        </a>
      </div>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 28px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
    .stat-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    .stat-card mat-card-content { display: flex; align-items: center; gap: 16px; padding: 24px !important; }
    .stat-icon { width: 52px; height: 52px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
    .stat-icon mat-icon { font-size: 26px; }
    .stat-value { font-size: 2rem; font-weight: 700; color: #111827; line-height: 1; }
    .stat-label { font-size: 0.82rem; color: #6B7280; margin-top: 4px; }
    .section-title { font-size: 1rem; font-weight: 600; color: #374151; margin: 0 0 16px; }
    .actions-row { display: flex; gap: 12px; flex-wrap: wrap; }
    .action-btn { background: #1E3A5F; color: white; border-radius: 8px; display: flex; align-items: center; gap: 6px; }
    .action-btn.outline { background: white; color: #1E3A5F; border: 1px solid #1E3A5F; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  private api = inject(ApiService);

  data: DashboardData | null = null;

  stats: { key: string; label: string; value: any; icon: string; bg: string; color: string }[] = [];

  ngOnInit(): void {
    this.api.getDashboard().subscribe({
      next: (res) => {
        this.data = res.data;
        this.buildStats();
      }
    });
  }

  private buildStats(): void {
    if (!this.data) return;
    this.stats = [
      { key: 'total',     label: 'Total Deliveries', value: this.data['totalDeliveries'] ?? this.data['total'],         icon: 'inventory_2',    bg: '#EFF6FF', color: '#1E3A5F' },
      { key: 'pending',   label: 'Pending',           value: this.data['pendingDeliveries'] ?? this.data['pending'],     icon: 'hourglass_empty', bg: '#FEF3C7', color: '#D97706' },
      { key: 'transit',   label: 'In Transit',        value: this.data['inTransitDeliveries'] ?? this.data['inTransit'], icon: 'local_shipping', bg: '#DBEAFE', color: '#2563EB' },
      { key: 'delivered', label: 'Delivered',         value: this.data['deliveredDeliveries'] ?? this.data['delivered'], icon: 'check_circle',   bg: '#D1FAE5', color: '#059669' },
      { key: 'hubs',      label: 'Total Hubs',        value: this.data['totalHubs'] ?? this.data['hubs'],               icon: 'warehouse',      bg: '#F3F4F6', color: '#6B7280' },
    ];
  }
}
