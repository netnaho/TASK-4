import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { OrderService } from '../../../core/services/order.service';
import { OrderDetailModel, OrderSummaryModel } from '../../../core/models/order.models';

@Component({
  selector: 'app-returns',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './returns.component.html',
  styleUrl: './returns.component.scss'
})
export class ReturnsComponent implements OnInit {
  orders: OrderSummaryModel[] = [];
  selectedOrder: OrderDetailModel | null = null;
  readonly form = this.fb.group({
    reasonCode: ['DAMAGED_GOODS', Validators.required],
    comments: [''],
    detail: ['Visible carton damage noted on receipt.', Validators.required],
    returnItemId: [null as number | null, Validators.required],
    returnQuantity: [0, [Validators.required, Validators.min(1)]],
    afterSalesItemId: [null as number | null, Validators.required]
  });

  constructor(private readonly fb: FormBuilder, private readonly orderService: OrderService) {}

  ngOnInit(): void {
    this.refresh();
  }

  load(orderId: number): void {
    this.orderService.getOrder(orderId).subscribe({
      next: (order) => {
        this.selectedOrder = order;
        this.form.patchValue({ returnItemId: null, returnQuantity: 0, afterSalesItemId: null });
      }
    });
  }

  createReturn(): void {
    if (!this.selectedOrder || this.form.controls.returnItemId.invalid || this.form.controls.returnQuantity.invalid) {
      this.form.controls.returnItemId.markAsTouched();
      this.form.controls.returnQuantity.markAsTouched();
      return;
    }
    const item = this.selectedOrder.items.find((candidate) => candidate.id === this.form.value.returnItemId);
    const quantity = Number(this.form.value.returnQuantity);
    if (!item || !Number.isFinite(quantity)) {
      return;
    }
    const maxReturnable = item.receivedQuantity - item.returnedQuantity;
    if (quantity < 1 || quantity > maxReturnable) {
      this.form.controls.returnQuantity.setErrors({ outOfRange: true });
      return;
    }
    this.orderService.createReturn(this.selectedOrder.id, {
      reasonCode: this.form.value.reasonCode,
      comments: this.form.value.comments,
      items: [{ orderItemId: item.id, quantity }]
    }).subscribe({ next: (order) => this.selectedOrder = order });
  }

  createAfterSalesCase(): void {
    if (!this.selectedOrder || this.form.controls.afterSalesItemId.invalid) {
      this.form.controls.afterSalesItemId.markAsTouched();
      return;
    }
    const item = this.selectedOrder.items.find((candidate) => candidate.id === this.form.value.afterSalesItemId);
    this.orderService.createAfterSalesCase(this.selectedOrder.id, {
      orderItemId: item?.id,
      reasonCode: this.form.value.reasonCode,
      structuredDetail: this.form.value.detail
    }).subscribe({ next: (order) => this.selectedOrder = order });
  }

  private refresh(): void {
    this.orderService.listOrders().subscribe({ next: (page) => (this.orders = page.content.filter((order) => ['PARTIALLY_SHIPPED', 'SHIPPED', 'RECEIVED'].includes(order.status))) });
  }
}
