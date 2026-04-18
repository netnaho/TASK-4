import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ReceiptsComponent } from './receipts.component';
import { OrderService } from '../../../core/services/order.service';

function shippedOrder(id: number) {
  return {
    id,
    orderNumber: `ORD-${id}`,
    status: 'SHIPPED',
    buyer: 'Buyer One',
    notes: '',
    paymentRecorded: true,
    createdAt: new Date().toISOString(),
    items: [
      {
        id: 100,
        productId: 1,
        sku: 'SKU-1',
        name: 'Item 1',
        unit: 'box',
        unitPrice: 12,
        orderedQuantity: 5,
        shippedQuantity: 5,
        receivedQuantity: 0,
        returnedQuantity: 0,
        remainingToShip: 0,
        remainingToReceive: 5,
        discrepancyFlag: false
      }
    ],
    shipments: [],
    receipts: [],
    returns: [],
    afterSalesCases: [],
    timeline: []
  };
}

describe('ReceiptsComponent', () => {
  it('loads orders with remaining receipts and posts a straight receipt without a dialog', async () => {
    const order = shippedOrder(1);
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({
        content: [{
          id: 1,
          orderNumber: 'ORD-1',
          status: 'SHIPPED',
          buyer: 'Buyer One',
          createdAt: '',
          totalOrderedQuantity: 5,
          totalShippedQuantity: 5,
          totalReceivedQuantity: 0,
          discrepancyOpen: false
        }],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
        first: true,
        last: true
      })),
      getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order)),
      createReceipt: jasmine.createSpy('createReceipt').and.returnValue(of(order))
    };
    const dialog = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [ReceiptsComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatDialog, useValue: dialog }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ReceiptsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.load(1);
    component.receiptItemControls[0].patchValue({ quantity: 5 });
    component.receivePartial(false);

    expect(orderService.createReceipt).toHaveBeenCalled();
    const args = orderService.createReceipt.calls.mostRecent().args as [number, any];
    expect(args[0]).toBe(1);
    expect(args[1].discrepancyConfirmed).toBeFalse();
    expect(dialog.open).not.toHaveBeenCalled();
  });

  it('opens the discrepancy dialog and only submits after explicit confirmation', async () => {
    const order = shippedOrder(2);
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({
        content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true
      })),
      getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order)),
      createReceipt: jasmine.createSpy('createReceipt').and.returnValue(of(order))
    };
    const afterClosed = of(true);
    const dialog = { open: jasmine.createSpy('open').and.returnValue({ afterClosed: () => afterClosed }) };

    await TestBed.configureTestingModule({
      imports: [ReceiptsComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatDialog, useValue: dialog }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ReceiptsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.load(2);
    // partial receipt triggers confirmation
    component.receiptItemControls[0].patchValue({ quantity: 2, discrepancyReason: 'short shipment' });
    component.receivePartial(true);

    expect(dialog.open).toHaveBeenCalled();
    expect(orderService.createReceipt).toHaveBeenCalled();
    const submitted = orderService.createReceipt.calls.mostRecent().args[1];
    expect(submitted.discrepancyConfirmed).toBeTrue();
  });

  it('ignores zero-quantity submissions and does not call the order service', async () => {
    const order = shippedOrder(3);
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({
        content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true
      })),
      getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order)),
      createReceipt: jasmine.createSpy('createReceipt')
    };
    await TestBed.configureTestingModule({
      imports: [ReceiptsComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatDialog, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ReceiptsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    component.load(3);

    component.receiptItemControls[0].patchValue({ quantity: 0 });
    component.receivePartial(false);

    expect(orderService.createReceipt).not.toHaveBeenCalled();
  });
});
