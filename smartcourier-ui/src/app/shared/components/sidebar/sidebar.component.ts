import { Component, Input, signal, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { CommonModule } from '@angular/common';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  id: string;
}

const CUSTOMER_NAV: NavItem[] = [
  { label: 'Dashboard',       icon: 'dashboard',        route: '/customer/dashboard', id: 'nav-cust-dashboard' },
  { label: 'Book Parcel',     icon: 'add_box',          route: '/customer/book',      id: 'nav-cust-book' },
  { label: 'Track Parcel',    icon: 'radar',            route: '/customer/track',     id: 'nav-cust-track' },
  { label: 'Booking History', icon: 'history',          route: '/customer/history',   id: 'nav-cust-history' },
];

const ADMIN_NAV: NavItem[] = [
  { label: 'Dashboard',  icon: 'bar_chart',        route: '/admin/dashboard',  id: 'nav-admin-dashboard' },
  { label: 'Parcels',    icon: 'inventory_2',      route: '/admin/parcels',    id: 'nav-admin-parcels' },
  { label: 'Customers',  icon: 'group',            route: '/admin/customers',  id: 'nav-admin-customers' },
  { label: 'Hubs',       icon: 'warehouse',        route: '/admin/hubs',       id: 'nav-admin-hubs' },
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, MatListModule, MatIconModule, MatButtonModule, MatDividerModule],
  template: `
    <aside class="sidebar" [class.collapsed]="collapsed()">
      <nav class="sidebar-nav">
        <mat-nav-list>
          <a mat-list-item
             *ngFor="let item of navItems"
             [routerLink]="item.route"
             routerLinkActive="active-link"
             [id]="item.id"
             class="nav-item">
            <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
            <span matListItemTitle *ngIf="!collapsed()">{{ item.label }}</span>
          </a>
        </mat-nav-list>
      </nav>
      <button mat-icon-button class="collapse-btn" (click)="toggleCollapse()" id="sidebar-collapse-btn">
        <mat-icon>{{ collapsed() ? 'chevron_right' : 'chevron_left' }}</mat-icon>
      </button>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 240px;
      min-height: calc(100vh - 64px);
      background: #fff;
      border-right: 1px solid #E5E7EB;
      display: flex;
      flex-direction: column;
      transition: width 0.25s ease;
      position: fixed;
      top: 64px;
      left: 0;
      bottom: 0;
      z-index: 900;
      box-shadow: 2px 0 8px rgba(0,0,0,0.04);
    }
    .sidebar.collapsed { width: 64px; }
    .sidebar-nav { flex: 1; padding-top: 8px; overflow: hidden; }
    .nav-item {
      margin: 2px 8px;
      border-radius: 8px;
      color: #374151;
      transition: background 0.15s;
    }
    .nav-item:hover { background: #F0F4FF; }
    .nav-item.active-link {
      background: #EFF6FF;
      color: #1E3A5F;
      font-weight: 600;
    }
    .nav-item.active-link mat-icon { color: #1E3A5F; }
    mat-icon { color: #6B7280; }
    .collapse-btn {
      margin: 8px auto;
      color: #6B7280;
    }
  `]
})
export class SidebarComponent {
  @Input() role: 'admin' | 'customer' = 'customer';

  private _collapsed = signal(false);
  collapsed = this._collapsed.asReadonly();

  get navItems(): NavItem[] {
    return this.role === 'admin' ? ADMIN_NAV : CUSTOMER_NAV;
  }

  toggleCollapse(): void {
    this._collapsed.update(v => !v);
  }
}
