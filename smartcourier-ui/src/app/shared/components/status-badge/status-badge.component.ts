import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { DeliveryStatus } from '../../../core/models/delivery.model';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule, MatChipsModule],
  template: `
    <span class="status-badge" [ngClass]="statusClass">
      <span class="status-dot"></span>
      {{ label }}
    </span>
  `,
  styles: [`
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      padding: 3px 10px;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.3px;
      text-transform: uppercase;
    }
    .status-dot {
      width: 6px; height: 6px;
      border-radius: 50%;
      background: currentColor;
      opacity: 0.7;
    }
    .status-booked     { background: #FEF3C7; color: #92400E; }
    .status-picked_up  { background: #E0E7FF; color: #3730A3; }
    .status-in_transit { background: #DBEAFE; color: #1D4ED8; }
    .status-delivered  { background: #D1FAE5; color: #065F46; }
    .status-cancelled  { background: #FEE2E2; color: #991B1B; }
  `]
})
export class StatusBadgeComponent {
  @Input() status: DeliveryStatus | string = 'BOOKED';

  get statusClass(): string {
    return `status-${(this.status ?? '').toLowerCase()}`;
  }

  get label(): string {
    const labels: Record<string, string> = {
      BOOKED: 'Booked',
      PICKED_UP: 'Picked Up',
      IN_TRANSIT: 'In Transit',
      DELIVERED: 'Delivered',
      CANCELLED: 'Cancelled'
    };
    return labels[this.status] ?? this.status;
  }
}
