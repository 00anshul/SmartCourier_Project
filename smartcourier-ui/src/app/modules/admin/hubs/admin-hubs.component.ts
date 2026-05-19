import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { ApiService } from '../../../core/services/api.service';
import { Hub } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-hubs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatDialogModule, MatSlideToggleModule, MatDividerModule],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">Hubs</h2>
        <p class="page-subtitle">Manage logistics hubs</p>
      </div>
      <button mat-flat-button class="primary-btn" (click)="showForm.set(!showForm())" id="create-hub-btn">
        <mat-icon>add</mat-icon> New Hub
      </button>
    </div>

    <!-- Create Hub Form -->
    <mat-card class="form-card" *ngIf="showForm()">
      <mat-card-header><mat-card-title>Create New Hub</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="hubForm" (ngSubmit)="createHub()" id="hub-form">
          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Hub Name</mat-label>
              <input matInput formControlName="name" placeholder="Mumbai Central Hub" id="hub-name">
              <mat-error *ngIf="hubForm.get('name')?.hasError('required')">Required</mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>City</mat-label>
              <input matInput formControlName="city" id="hub-city">
              <mat-error *ngIf="hubForm.get('city')?.hasError('required')">Required</mat-error>
            </mat-form-field>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>State</mat-label>
              <input matInput formControlName="state" id="hub-state">
              <mat-error *ngIf="hubForm.get('state')?.hasError('required')">Required</mat-error>
            </mat-form-field>
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Pincode</mat-label>
              <input matInput formControlName="pincode" id="hub-pincode">
              <mat-error *ngIf="hubForm.get('pincode')?.hasError('required')">Required</mat-error>
            </mat-form-field>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="half-width">
              <mat-label>Contact Phone</mat-label>
              <input matInput formControlName="contactPhone" id="hub-phone">
              <mat-error *ngIf="hubForm.get('contactPhone')?.hasError('required')">Required</mat-error>
            </mat-form-field>
          </div>
          <div class="form-actions">
            <button mat-button type="button" (click)="showForm.set(false)">Cancel</button>
            <button mat-flat-button class="primary-btn" type="submit" [disabled]="hubForm.invalid || creating()" id="hub-submit-btn">
              {{ creating() ? 'Creating...' : 'Create Hub' }}
            </button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>

    <!-- Hubs Grid -->
    <div class="hubs-grid">
      <mat-card class="hub-card" *ngFor="let hub of hubs()" [id]="'hub-card-' + hub.id">
        <mat-card-content>
          <div class="hub-header">
            <div class="hub-icon" [class.inactive]="!hub.isActive">
              <mat-icon>warehouse</mat-icon>
            </div>
            <div class="hub-info">
              <div class="hub-name">{{ hub.name }}</div>
              <div class="hub-city"><mat-icon>place</mat-icon>{{ hub.city }}, {{ hub.state }} - {{ hub.pincode }}</div>
            </div>
            <mat-slide-toggle [checked]="hub.isActive" (change)="toggleHub(hub)" [id]="'hub-toggle-' + hub.id" color="primary"></mat-slide-toggle>
          </div>
          <mat-divider></mat-divider>
          <div class="hub-details">
            <div class="hub-detail"><mat-icon>phone</mat-icon>{{ hub.contactPhone }}</div>
            <span class="hub-status" [class.active]="hub.isActive">{{ hub.isActive ? 'Active' : 'Inactive' }}</span>
          </div>
        </mat-card-content>
      </mat-card>

      <div class="empty-state" *ngIf="hubs().length === 0">
        <mat-icon>warehouse</mat-icon>
        <p>No hubs created yet</p>
        <button mat-flat-button class="primary-btn" (click)="showForm.set(true)">Create your first hub</button>
      </div>
    </div>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .primary-btn { background: #1E3A5F; color: white; border-radius: 8px; display: flex; align-items: center; gap: 4px; }
    .form-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; margin-bottom: 24px; }
    mat-card-content { padding: 20px !important; }
    .form-row { display: flex; gap: 12px; }
    .half-width { flex: 1; min-width: 0; }
    .full-width { width: 100%; }
    .form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px; }
    .hubs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
    .hub-card { border-radius: 12px !important; box-shadow: 0 2px 8px rgba(0,0,0,0.06) !important; }
    .hub-header { display: flex; align-items: center; gap: 12px; padding-bottom: 12px; }
    .hub-icon { width: 44px; height: 44px; border-radius: 10px; background: #EFF6FF; display: flex; align-items: center; justify-content: center; }
    .hub-icon mat-icon { color: #1E3A5F; }
    .hub-icon.inactive { background: #F3F4F6; }
    .hub-icon.inactive mat-icon { color: #9CA3AF; }
    .hub-info { flex: 1; }
    .hub-name { font-weight: 600; color: #111827; }
    .hub-city { display: flex; align-items: center; gap: 2px; font-size: 0.8rem; color: #6B7280; }
    .hub-city mat-icon { font-size: 14px; width: 14px; height: 14px; }
    .hub-details { padding-top: 12px; display: flex; flex-direction: column; gap: 6px; }
    .hub-detail { display: flex; align-items: center; gap: 6px; font-size: 0.82rem; color: #374151; }
    .hub-detail mat-icon { font-size: 16px; width: 16px; height: 16px; color: #9CA3AF; }
    .hub-status { padding: 3px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; background: #FEE2E2; color: #991B1B; width: fit-content; margin-top: 4px; }
    .hub-status.active { background: #D1FAE5; color: #065F46; }
    .empty-state { grid-column: 1/-1; display: flex; flex-direction: column; align-items: center; padding: 60px; color: #9CA3AF; gap: 12px; }
    .empty-state mat-icon { font-size: 56px; width: 56px; height: 56px; }
  `]
})
export class AdminHubsComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  hubs = signal<Hub[]>([]);
  showForm = signal(false);
  creating = signal(false);

  hubForm = this.fb.group({
    name: ['', Validators.required],
    city: ['', Validators.required],
    state: ['', Validators.required],
    pincode: ['', Validators.required],
    contactPhone: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadHubs();
  }

  loadHubs(): void {
    this.api.getAllHubs().subscribe({ next: (res) => { this.hubs.set(res.data ?? []); } });
  }

  createHub(): void {
    if (this.hubForm.invalid) return;
    this.creating.set(true);
    this.api.createHub(this.hubForm.value as any).subscribe({
      next: (res) => {
        this.hubs.update(h => [res.data, ...h]);
        this.hubForm.reset();
        this.showForm.set(false);
        this.snackBar.open('Hub created!', '✓', { duration: 2000, panelClass: ['sc-snackbar-success'] });
        this.creating.set(false);
      },
      error: () => { this.creating.set(false); }
    });
  }

  toggleHub(hub: Hub): void {
    this.api.toggleHub(hub.id).subscribe({
      next: (res) => {
        this.hubs.update(hubsList => {
          const idx = hubsList.findIndex(h => h.id === hub.id);
          if (idx !== -1) {
            hubsList[idx] = res.data;
          }
          return [...hubsList];
        });
        this.snackBar.open(`Hub ${res.data.isActive ? 'activated' : 'deactivated'}`, '✓', { duration: 2000 });
      }
    });
  }
}
