import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderService } from '../../../core/services/order.service';
import { OrderSummaryModel, ProductCatalogModel } from '../../../core/models/order.models';
import { OrderStatusBadgeComponent } from '../shared/order-status-badge.component';

@Component({
  selector: 'app-order-workspace',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatTableModule, MatPaginatorModule, OrderStatusBadgeComponent],
  templateUrl: './order-workspace.component.html',
  styleUrl: './order-workspace.component.scss'
})
export class OrderWorkspaceComponent implements OnInit {
  catalog: ProductCatalogModel[] = [];
  orders: OrderSummaryModel[] = [];
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  isLoading = true;
  loadError = '';
  isCreatingDraft = false;
  readonly reviewSubmittingIds = new Set<number>();
  readonly cancelSubmittingIds = new Set<number>();
  readonly columns = ['orderNumber', 'status', 'ordered', 'shipped', 'received'];

  readonly form = this.fb.group({
    notes: [''],
    items: this.fb.array([this.createItemRow()])
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly orderService: OrderService,
    private readonly snackBar: MatSnackBar,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  addRow(): void {
    this.items.push(this.createItemRow());
  }

  submit(): void {
    if (this.isCreatingDraft) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const payload = {
      notes: this.form.value.notes,
      items: this.items.getRawValue().filter((item) => item.productId).map((item) => ({ productId: item.productId, quantity: Number(item.quantity) }))
    };
    this.isCreatingDraft = true;
    this.orderService.createOrder(payload).pipe(
      finalize(() => (this.isCreatingDraft = false))
    ).subscribe({
      next: (order) => {
        this.snackBar.open('Order created', 'Dismiss', { duration: 2500 });
        this.router.navigate(['/orders', order.id]);
      }
    });
  }

  submitForReview(orderId: number): void {
    if (this.reviewSubmittingIds.has(orderId)) {
      return;
    }
    this.reviewSubmittingIds.add(orderId);
    this.orderService.submitForReview(orderId).pipe(
      finalize(() => this.reviewSubmittingIds.delete(orderId))
    ).subscribe({
      next: () => {
        this.snackBar.open('Order submitted for review', 'Dismiss', { duration: 2500 });
        this.reload();
      }
    });
  }

  cancel(orderId: number): void {
    if (this.cancelSubmittingIds.has(orderId)) {
      return;
    }
    this.cancelSubmittingIds.add(orderId);
    this.orderService.cancelOrder(orderId).pipe(
      finalize(() => this.cancelSubmittingIds.delete(orderId))
    ).subscribe({
      next: () => {
        this.snackBar.open('Order canceled', 'Dismiss', { duration: 2500 });
        this.reload();
      }
    });
  }

  openOrder(orderId: number): void {
    this.router.navigate(['/orders', orderId]);
  }

  isSubmitForReviewPending(orderId: number): boolean {
    return this.reviewSubmittingIds.has(orderId);
  }

  isCancelPending(orderId: number): boolean {
    return this.cancelSubmittingIds.has(orderId);
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.reload();
  }

  private reload(): void {
    this.isLoading = true;
    this.loadError = '';
    forkJoin({
      catalog: this.orderService.listCatalog(),
      orders: this.orderService.listOrders({ page: this.pageIndex, size: this.pageSize })
    }).pipe(
      catchError(() => {
        this.loadError = 'Order workspace data could not be loaded right now.';
        this.isLoading = false;
        return of({ catalog: [] as ProductCatalogModel[], orders: { content: [] as OrderSummaryModel[], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true } });
      })
    ).subscribe({
      next: ({ catalog, orders }) => {
        this.catalog = catalog;
        this.orders = orders.content;
        this.totalElements = orders.totalElements;
        this.isLoading = false;
      }
    });
  }

  private createItemRow() {
    return this.fb.group({
      productId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });
  }
}
