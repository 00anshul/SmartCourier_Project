import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-customer-layout',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent, LoadingSpinnerComponent],
  template: `
    <app-navbar></app-navbar>
    <app-loading-spinner></app-loading-spinner>
    <div class="app-shell">
      <app-sidebar role="customer"></app-sidebar>
      <main class="main-content" id="customer-main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-shell { display: flex; padding-top: 64px; min-height: 100vh; }
    .main-content { flex: 1; margin-left: 240px; padding: 32px; background: #F8FAFC; min-height: calc(100vh - 64px); transition: margin-left 0.25s; }
    @media (max-width: 768px) { .main-content { margin-left: 0; padding: 16px; } }
  `]
})
export class CustomerLayoutComponent {}
