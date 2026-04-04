import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { OrderService } from '../../../core/services/order.service';
import { OrderDetailModel, OrderSummaryModel } from '../../../core/models/order.models';
import { DiscrepancyDialogComponent } from './discrepancy-dialog.component';

@Component({
  selector: 'app-receipts',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  templateUrl: './receipts.component.html',
  styleUrl: './receipts.component.scss'
})
export class ReceiptsComponent implements OnInit {
  orders: OrderSummaryModel[] = [];
  selectedOrder: OrderDetailModel | null = null;
  readonly receiptForm = this.fb.group({ notes: [''], items: this.fb.array<FormGroup>([]) });

  constructor(private readonly fb: FormBuilder, private readonly orderService: OrderService, private readonly dialog: MatDialog) {}

  ngOnInit(): void {
    this.refresh();
  }

  load(orderId: number): void {
    this.orderService.getOrder(orderId).subscribe({
      next: (order) => {
        this.selectedOrder = order;
        this.resetReceiptItems(order);
      }
    });
  }

  receivePartial(discrepancy = false): void {
    if (!this.selectedOrder) {
      return;
    }
    const items = this.receiptItemControls
      .map((group) => ({
        orderItemId: group.controls['orderItemId'].value,
        quantity: Number(group.controls['quantity'].value),
        discrepancyReason: group.controls['discrepancyReason'].value,
        remainingToReceive: Number(group.controls['remainingToReceive'].value),
        remainingOrdered: Number(group.controls['remainingOrdered'].value)
      }))
      .filter((item) => item.quantity > 0)
      .map((item) => ({ ...item, discrepancyReason: item.discrepancyReason || undefined }));
    if (items.length === 0) {
      this.receiptItems.markAllAsTouched();
      return;
    }

    const requiresConfirmation = discrepancy || items.some((item) => item.quantity !== item.remainingToReceive || item.remainingToReceive !== item.remainingOrdered || !!item.discrepancyReason);

    const submit = () => this.orderService.createReceipt(this.selectedOrder!.id, {
      items: items.map(({ orderItemId, quantity, discrepancyReason }) => ({ orderItemId, quantity, discrepancyReason })),
      notes: this.receiptForm.value.notes,
      discrepancyConfirmed: requiresConfirmation
    }).subscribe({
      next: (order) => {
        this.selectedOrder = order;
        this.resetReceiptItems(order);
      }
    });

    if (requiresConfirmation) {
      this.dialog.open(DiscrepancyDialogComponent).afterClosed().subscribe((confirmed) => confirmed && submit());
      return;
    }
    submit();
  }

  get receiptItems(): FormArray<FormGroup> {
    return this.receiptForm.controls['items'] as FormArray<FormGroup>;
  }

  get receiptItemControls(): FormGroup[] {
    return this.receiptItems.controls as FormGroup[];
  }

  private resetReceiptItems(order: OrderDetailModel): void {
    const next = order.items
      .filter((item) => item.remainingToReceive > 0)
      .map((item) =>
        this.fb.group({
          orderItemId: this.fb.nonNullable.control(item.id),
          sku: this.fb.nonNullable.control(item.sku),
          remainingToReceive: this.fb.nonNullable.control(item.remainingToReceive),
          remainingOrdered: this.fb.nonNullable.control(item.orderedQuantity - item.receivedQuantity),
          quantity: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0), Validators.max(item.remainingToReceive)]),
          discrepancyReason: this.fb.nonNullable.control('')
        })
      );
    this.receiptForm.setControl('items', this.fb.array(next));
  }

  private refresh(): void {
    this.orderService.listOrders().subscribe({ next: (page) => (this.orders = page.content.filter((order) => ['PARTIALLY_SHIPPED', 'SHIPPED'].includes(order.status))) });
  }
}
