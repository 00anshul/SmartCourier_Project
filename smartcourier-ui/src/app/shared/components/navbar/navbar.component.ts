import { Component, inject } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, MatDividerModule],
  template: `
    <mat-toolbar class="navbar">
      <div class="navbar-brand">
        <mat-icon class="brand-icon">local_shipping</mat-icon>
        <span class="brand-name">SmartCourier</span>
      </div>
      <span class="spacer"></span>
      <div class="navbar-user" *ngIf="auth.isAuthenticated()">
        <span class="user-greeting">Hello, {{ auth.getFullName() }}</span>
        <button mat-icon-button [matMenuTriggerFor]="userMenu" id="navbar-user-menu">
          <mat-icon>account_circle</mat-icon>
        </button>
        <mat-menu #userMenu="matMenu">
          <button mat-menu-item disabled>
            <mat-icon>badge</mat-icon>
            <span>{{ auth.getRole() }}</span>
          </button>
          <mat-divider></mat-divider>
          <button mat-menu-item (click)="auth.logout()" id="navbar-logout-btn">
            <mat-icon>logout</mat-icon>
            <span>Logout</span>
          </button>
        </mat-menu>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .navbar {
      background: #1E3A5F;
      color: white;
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 1000;
      box-shadow: 0 2px 12px rgba(0,0,0,0.15);
    }
    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: default;
    }
    .brand-icon { color: #60A5FA; font-size: 28px; }
    .brand-name { font-size: 1.3rem; font-weight: 700; letter-spacing: 0.5px; }
    .spacer { flex: 1; }
    .navbar-user { display: flex; align-items: center; gap: 8px; }
    .user-greeting { font-size: 0.875rem; opacity: 0.85; }
    button[mat-icon-button] { color: white; }
  `]
})
export class NavbarComponent {
  auth = inject(AuthService);
}
