import { Component, Input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { TraceabilityEventModel } from '../../../core/models/order.models';

@Component({
  selector: 'app-order-timeline',
  standalone: true,
  imports: [CommonModule, DatePipe, MatCardModule],
  templateUrl: './order-timeline.component.html',
  styleUrl: './order-timeline.component.scss'
})
export class OrderTimelineComponent {
  @Input({ required: true }) events: TraceabilityEventModel[] = [];
}
