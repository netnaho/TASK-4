import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, of } from 'rxjs';
import { OrderService } from '../../../core/services/order.service';
import { OrderDetailModel } from '../../../core/models/order.models';
import { OrderDetailCardComponent } from '../shared/order-detail-card.component';
import { OrderTimelineComponent } from '../shared/order-timeline.component';
import { CriticalActionService } from '../../../core/services/critical-action.service';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, OrderDetailCardComponent, OrderTimelineComponent],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
})
export class OrderDetailComponent implements OnInit {
  order: OrderDetailModel | null = null;
  loadError = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly orderService: OrderService,
    private readonly criticalActionService: CriticalActionService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getOrder(orderId).pipe(
      catchError(() => {
        this.loadError = 'Order traceability could not be loaded right now.';
        return of(null);
      })
    ).subscribe({ next: (order) => (this.order = order) });
  }

  requestProtectedCancellation(): void {
    if (!this.order) {
      return;
    }
    this.criticalActionService.create({
      requestType: 'ORDER_CANCELLATION_AFTER_APPROVAL',
      targetType: 'ORDER',
      targetId: this.order.id,
      justification: 'Cancellation requested after approval; requires critical action dual approval.'
    }).subscribe({ next: () => this.snackBar.open('Critical cancellation request created', 'Dismiss', { duration: 2200 }) });
  }
}
