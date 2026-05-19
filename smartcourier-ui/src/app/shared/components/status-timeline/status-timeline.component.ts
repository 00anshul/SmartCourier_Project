import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { TrackingEvent } from '../../../core/models/tracking.model';

interface TimelineStep {
  label: string;
  icon: string;
  statusKey: string;
}

const STEPS: TimelineStep[] = [
  { label: 'Booked',      icon: 'bookmark_added', statusKey: 'PENDING' },
  { label: 'Picked Up',   icon: 'inventory',      statusKey: 'PICKED_UP' },
  { label: 'In Transit',  icon: 'local_shipping', statusKey: 'IN_TRANSIT' },
  { label: 'Delivered',   icon: 'check_circle',   statusKey: 'DELIVERED' },
];

@Component({
  selector: 'app-status-timeline',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="timeline-wrapper">
      <div class="timeline">
        <div *ngFor="let step of steps; let i = index; let last = last"
             class="timeline-step"
             [class.completed]="isCompleted(step)"
             [class.active]="isActive(step)">
          <div class="step-icon">
            <mat-icon>{{ step.icon }}</mat-icon>
          </div>
          <div class="step-label">
            {{ step.label }}
            <div class="step-location" *ngIf="getEventLocation(step) as loc">{{ loc }}</div>
          </div>
          <div class="step-time" *ngIf="getEventTime(step) as t">{{ t + 'Z' | date:'dd MMM, HH:mm' }}</div>
          <div class="step-connector" *ngIf="!last"></div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .timeline-wrapper { padding: 20px 0; }
    .timeline {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      position: relative;
    }
    .timeline-step {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;
      position: relative;
      text-align: center;
    }
    .step-icon {
      width: 48px; height: 48px;
      border-radius: 50%;
      background: #E5E7EB;
      display: flex; align-items: center; justify-content: center;
      transition: all 0.3s;
      z-index: 1;
    }
    .step-icon mat-icon { color: #9CA3AF; font-size: 22px; }
    .timeline-step.completed .step-icon { background: #1E3A5F; }
    .timeline-step.completed .step-icon mat-icon { color: white; }
    .timeline-step.active .step-icon { background: #3B82F6; box-shadow: 0 0 0 4px rgba(59,130,246,0.25); }
    .timeline-step.active .step-icon mat-icon { color: white; }
    .step-label {
      margin-top: 8px;
      font-size: 0.8rem;
      font-weight: 600;
      color: #374151;
    }
    .step-location { font-size: 0.7rem; font-weight: normal; color: #6B7280; margin-top: 2px; }
    .timeline-step.completed .step-label, .timeline-step.active .step-label { color: #1E3A5F; }
    .step-time { font-size: 0.7rem; color: #6B7280; margin-top: 4px; }
    .step-connector {
      position: absolute;
      top: 24px;
      left: 50%;
      width: 100%;
      height: 2px;
      background: #E5E7EB;
      z-index: 0;
    }
    .timeline-step.completed .step-connector { background: #1E3A5F; }
  `]
})
export class StatusTimelineComponent {
  @Input() currentStatus: string = 'PENDING';
  @Input() events: TrackingEvent[] = [];

  steps = STEPS;

  private readonly ORDER = ['PENDING', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED'];

  isCompleted(step: TimelineStep): boolean {
    const current = this.ORDER.indexOf(this.currentStatus);
    const stepIdx = this.ORDER.indexOf(step.statusKey);
    return stepIdx < current;
  }

  isActive(step: TimelineStep): boolean {
    return this.currentStatus === step.statusKey;
  }

  getEventTime(step: TimelineStep): string | null {
    const event = this.events.find(e => e.status === step.statusKey);
    return event?.eventTime ?? null;
  }

  getEventLocation(step: TimelineStep): string | null {
    const event = this.events.find(e => e.status === step.statusKey);
    if (!event || !event.locationDescription) return null;
    // Don't show the generic "Status updated to X" message as a location
    if (event.locationDescription.startsWith('Status updated to')) return null;
    if (event.locationDescription.startsWith('Status updated at ')) {
      return event.locationDescription.replace('Status updated at ', '');
    }
    return event.locationDescription;
  }
}
