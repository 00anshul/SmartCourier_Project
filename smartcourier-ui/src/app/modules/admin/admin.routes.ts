import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './admin-layout.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', loadComponent: () => import('./dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'parcels',   loadComponent: () => import('./parcels/admin-parcels.component').then(m => m.AdminParcelsComponent) },
      { path: 'customers', loadComponent: () => import('./customers/admin-customers.component').then(m => m.AdminCustomersComponent) },
      { path: 'hubs',      loadComponent: () => import('./hubs/admin-hubs.component').then(m => m.AdminHubsComponent) },
      { path: '',          redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
