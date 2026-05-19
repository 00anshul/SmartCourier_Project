import { Routes } from '@angular/router';
import { CustomerLayoutComponent } from './customer-layout.component';

export const CUSTOMER_ROUTES: Routes = [
  {
    path: '',
    component: CustomerLayoutComponent,
    children: [
      { path: 'dashboard', loadComponent: () => import('./dashboard/customer-dashboard.component').then(m => m.CustomerDashboardComponent) },
      { path: 'book', loadComponent: () => import('./book-parcel/book-parcel.component').then(m => m.BookParcelComponent) },
      { path: 'track', loadComponent: () => import('./track-parcel/track-parcel.component').then(m => m.TrackParcelComponent) },
      { path: 'history', loadComponent: () => import('./history/booking-history.component').then(m => m.BookingHistoryComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
