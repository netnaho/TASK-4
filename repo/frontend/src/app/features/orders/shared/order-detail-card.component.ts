import { Component, Input } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { OrderDetailModel } from '../../../core/models/order.models';
import { OrderStatusBadgeComponent } from './order-status-badge.component';

@Component({
  selector: 'app-order-detail-card',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, RouterLink, MatCardModule, MatTableModule, MatButtonModule, OrderStatusBadgeComponent],
  templateUrl: './order-detail-card.component.html',
  styleUrl: './order-detail-card.component.scss'
})
export class OrderDetailCardComponent {
  @Input() order: OrderDetailModel | null = null;

  readonly columns = ['sku', 'name', 'ordered', 'shipped', 'received', 'remaining'];
}
