import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Subject, of } from 'rxjs';
import { FulfillmentOrdersComponent } from './fulfillment-orders.component';
import { OrderService } from '../../../core/services/order.service';

function partialShipmentOrder() {
  return {
    id: 12,
    orderNumber: 'ORD-123',
    status: 'PARTIALLY_SHIPPED',
    buyer: 'Buyer One',
    notes: 'Warehouse order',
    paymentRecorded: true,
    createdAt: new Date().toISOString(),
    items: [
      {
        id: 501,
        productId: 1,
        sku: 'SKU-1',
        name: 'Item 1',
        unit: 'box',
        unitPrice: 12,
        orderedQuantity: 4,
        shippedQuantity: 2,
        receivedQuantity: 0,
        returnedQuantity: 0,
        remainingToShip: 2,
        remainingToReceive: 2,
        discrepancyFlag: false
      }
    ],
    shipments: [{ id: 1, shipmentNumber: 'SHP-1', actor: 'Fulfillment One', shippedAt: new Date().toISOString(), notes: 'Wave 1', items: [] }],
    receipts: [],
    returns: [],
    afterSalesCases: [],
    timeline: []
  };
}

describe('FulfillmentOrdersComponent', () => {
  it('shows explicit second-shipment guidance for partially shipped orders', async () => {
    const order = partialShipmentOrder();
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({ content: [{ id: 12, orderNumber: 'ORD-123', status: 'PARTIALLY_SHIPPED', buyer: 'Buyer One', createdAt: '', totalOrderedQuantity: 4, totalShippedQuantity: 2, totalReceivedQuantity: 0, discrepancyOpen: false }], totalElements: 1, totalPages: 1, number: 0, size: 20, first: true, last: true })),
      getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order)),
      pickPack: jasmine.createSpy('pickPack'),
      createShipment: jasmine.createSpy('createShipment')
    };

    await TestBed.configureTestingModule({
      imports: [FulfillmentOrdersComponent, NoopAnimationsModule],
      providers: [{ provide: OrderService, useValue: orderService }]
    }).compileComponents();

    const fixture = TestBed.createComponent(FulfillmentOrdersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.load(12);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Shipment 2 is ready. 2 unit(s) remain to ship across 1 line(s).');
  });

  it('prevents duplicate shipment submissions while a shipment request is active', async () => {
    const order = partialShipmentOrder();
    const createShipment$ = new Subject<any>();
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order)),
      pickPack: jasmine.createSpy('pickPack'),
      createShipment: jasmine.createSpy('createShipment').and.returnValue(createShipment$)
    };

    await TestBed.configureTestingModule({
      imports: [FulfillmentOrdersComponent, NoopAnimationsModule],
      providers: [{ provide: OrderService, useValue: orderService }]
    }).compileComponents();

    const fixture = TestBed.createComponent(FulfillmentOrdersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.load(12);
    component.shipmentItemControls[0].patchValue({ quantity: 1 });
    component.createShipment();
    component.createShipment();

    expect(orderService.createShipment).toHaveBeenCalledTimes(1);
    expect(component.isCreatingShipment).toBeTrue();
  });
});
