import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, of } from 'rxjs';
import { OrderService } from '../../../core/services/order.service';
import { OrderSummaryModel } from '../../../core/models/order.models';

@Component({
  selector: 'app-finance-orders',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './finance-orders.component.html',
  styleUrl: './finance-orders.component.scss'
})
export class FinanceOrdersComponent implements OnInit {
  orders: OrderSummaryModel[] = [];
  loadError = '';
  readonly form = this.fb.group({ referenceNumber: ['PAY-001', Validators.required], amount: [100, Validators.required] });

  constructor(private readonly fb: FormBuilder, private readonly orderService: OrderService, private readonly snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.refresh();
  }

  record(orderId: number): void {
    this.orderService.recordPayment(orderId, this.form.getRawValue()).subscribe({
      next: () => this.refresh(),
      error: () => this.snackBar.open('Payment recording failed. Verify values and retry.', 'Dismiss', { duration: 2800 })
    });
  }

  private refresh(): void {
    this.loadError = '';
    this.orderService.listOrders({ status: 'APPROVED' }).pipe(
      catchError(() => {
        this.orders = [];
        this.loadError = 'Approved orders could not be loaded right now.';
        return of({ content: [] as OrderSummaryModel[], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true });
      })
    ).subscribe({ next: (page) => (this.orders = page.content) });
  }
}
