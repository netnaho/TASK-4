import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [MatCardModule, MatChipsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  readonly cards = [
    { label: 'Orders in Pipeline', value: '28', status: 'Active', note: '+6% this week' },
    { label: 'Documents in Review', value: '14', status: 'Pending', note: 'SOP and packing slips' },
    { label: 'Field Check-ins Today', value: '9', status: 'Synced', note: 'All submissions archived' },
    { label: 'Approvals Awaiting Action', value: '6', status: 'Requires Attention', note: 'Dual-control tasks building up' }
  ];

  readonly queue = [
    '2 procurement orders are waiting for quality review.',
    '1 shipment batch is ready for pick and pack handoff.',
    '3 receipts contain partial fulfillment and need buyer confirmation.'
  ];

  readonly highlights = [
    { title: 'Order lifecycle health', value: '94%', description: 'State transitions remain compliant across active orders.' },
    { title: 'Exceptions under watch', value: '3', description: 'Temperature excursion and damaged goods cases open.' }
  ];
}
