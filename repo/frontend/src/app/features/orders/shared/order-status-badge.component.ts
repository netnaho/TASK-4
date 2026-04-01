import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-order-status-badge',
  standalone: true,
  imports: [CommonModule, MatChipsModule],
  template: '<mat-chip [ngClass]="statusClass(status)">{{ status.replaceAll("_", " ") }}</mat-chip>',
  styleUrl: './order-status-badge.component.scss'
})
export class OrderStatusBadgeComponent {
  @Input({ required: true }) status = 'CREATED';

  statusClass(status: string): string {
    return `status-${status.toLowerCase().replaceAll('_', '-')}`;
  }
}
