import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { OrderService } from '../../../core/services/order.service';
import { OrderDetailModel, OrderSummaryModel } from '../../../core/models/order.models';

@Component({
  selector: 'app-fulfillment-orders',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './fulfillment-orders.component.html',
  styleUrl: './fulfillment-orders.component.scss'
})
export class FulfillmentOrdersComponent implements OnInit {
  orders: OrderSummaryModel[] = [];
  selectedOrder: OrderDetailModel | null = null;
  isCreatingShipment = false;
  readonly pickPackInFlight = new Set<number>();
  shipmentForm: FormGroup = this.fb.group({ notes: [''], items: this.fb.array<FormGroup>([]) });

  constructor(private readonly fb: FormBuilder, private readonly orderService: OrderService) {}

  ngOnInit(): void {
    this.refresh();
  }

  startPickPack(orderId: number): void {
    if (this.pickPackInFlight.has(orderId)) {
      return;
    }
    this.pickPackInFlight.add(orderId);
    this.orderService.pickPack(orderId).pipe(
      finalize(() => this.pickPackInFlight.delete(orderId))
    ).subscribe({
      next: (order) => {
        this.selectedOrder = order;
        this.resetShipmentItems(order);
        this.refresh();
      }
    });
  }

  load(orderId: number): void {
    this.orderService.getOrder(orderId).subscribe({
      next: (order) => {
        this.selectedOrder = order;
        this.resetShipmentItems(order);
      }
    });
  }

  createShipment(): void {
    if (!this.selectedOrder || !this.canCreateShipment() || this.isCreatingShipment) {
      return;
    }
    const rawItems = this.shipmentItemControls
      .map((group) => ({
        orderItemId: group.controls['orderItemId'].value,
        quantity: Number(group.controls['quantity'].value)
      }))
      .filter((item) => item.quantity > 0);
    if (rawItems.length === 0 || rawItems.some((item) => !Number.isFinite(item.quantity))) {
      this.shipmentItems.markAllAsTouched();
      return;
    }
    this.isCreatingShipment = true;
    this.orderService.createShipment(this.selectedOrder.id, { notes: this.shipmentForm.value.notes, items: rawItems }).pipe(
      finalize(() => (this.isCreatingShipment = false))
    ).subscribe({
      next: (order) => {
        this.selectedOrder = order;
        this.resetShipmentItems(order);
        this.refresh();
      }
    });
  }

  canCreateShipment(): boolean {
    return !!this.selectedOrder
      && ['PICK_PACK', 'PARTIALLY_SHIPPED'].includes(this.selectedOrder.status)
      && this.shipmentItemControls.length > 0;
  }

  shipmentSequenceLabel(): string {
    if (!this.selectedOrder) {
      return 'Shipment builder';
    }
    return `Shipment ${this.selectedOrder.shipments.length + 1}`;
  }

  shipmentStatusMessage(): string {
    if (!this.selectedOrder) {
      return 'Select an order to create a partial shipment.';
    }

    const remainingUnits = this.selectedOrder.items.reduce((total, item) => total + item.remainingToShip, 0);
    const remainingLines = this.selectedOrder.items.filter((item) => item.remainingToShip > 0).length;

    if (remainingUnits === 0 || this.selectedOrder.status === 'SHIPPED') {
      return 'All approved quantities have already been shipped for this order.';
    }

    if (this.selectedOrder.status === 'PAYMENT_RECORDED') {
      return 'Start pick and pack before creating the first shipment.';
    }

    if (this.selectedOrder.shipments.length === 0) {
      return `Create the first shipment for ${remainingUnits} unit(s) across ${remainingLines} line(s).`;
    }

    return `${this.shipmentSequenceLabel()} is ready. ${remainingUnits} unit(s) remain to ship across ${remainingLines} line(s).`;
  }

  openShipmentLineCount(): number {
    return this.selectedOrder?.items.filter((item) => item.remainingToShip > 0).length ?? 0;
  }

  isPickPackPending(orderId: number): boolean {
    return this.pickPackInFlight.has(orderId);
  }

  get shipmentItems(): FormArray<FormGroup> {
    return this.shipmentForm.controls['items'] as FormArray<FormGroup>;
  }

  get shipmentItemControls(): FormGroup[] {
    return this.shipmentItems.controls as FormGroup[];
  }

  private resetShipmentItems(order: OrderDetailModel): void {
    const next = order.items
      .filter((item) => item.remainingToShip > 0)
      .map((item) =>
        this.fb.group({
          orderItemId: this.fb.nonNullable.control(item.id),
          quantity: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0), Validators.max(item.remainingToShip)]),
          sku: this.fb.nonNullable.control(item.sku),
          remainingToShip: this.fb.nonNullable.control(item.remainingToShip)
        })
      );
    this.shipmentForm.setControl('items', this.fb.array(next));
  }

  private refresh(): void {
    this.orderService.listOrders().subscribe({ next: (page) => (this.orders = page.content.filter((order) => ['PAYMENT_RECORDED', 'PICK_PACK', 'PARTIALLY_SHIPPED'].includes(order.status))) });
  }
}
