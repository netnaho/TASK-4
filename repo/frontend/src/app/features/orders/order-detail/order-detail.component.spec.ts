import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { OrderDetailComponent } from './order-detail.component';
import { OrderService } from '../../../core/services/order.service';
import { CriticalActionService } from '../../../core/services/critical-action.service';

function fullOrder(id: number) {
  return {
    id,
    orderNumber: `ORD-${id}`,
    status: 'APPROVED',
    buyer: 'Buyer One',
    notes: '',
    paymentRecorded: false,
    createdAt: '',
    items: [],
    shipments: [],
    receipts: [],
    returns: [],
    afterSalesCases: [],
    timeline: []
  };
}

describe('OrderDetailComponent', () => {
  function configure(orderService: any, criticalActionService: any, snackBar: any, routeId = '1') {
    return TestBed.configureTestingModule({
      imports: [OrderDetailComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: CriticalActionService, useValue: criticalActionService },
        { provide: MatSnackBar, useValue: snackBar },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => routeId } } } }
      ]
    }).compileComponents();
  }

  it('loads order by route id and renders detail', async () => {
    const order = fullOrder(42);
    const orderService = {
      getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order))
    };
    const criticalActionService = { create: jasmine.createSpy('create') };
    const snackBar = { open: jasmine.createSpy('open') };

    await configure(orderService, criticalActionService, snackBar, '42');
    const fixture = TestBed.createComponent(OrderDetailComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(orderService.getOrder).toHaveBeenCalledWith(42);
    expect(component.order).toEqual(order);
    expect(component.loadError).toBe('');
  });

  it('sets loadError when the order cannot be fetched', async () => {
    const orderService = {
      getOrder: jasmine.createSpy('getOrder').and.returnValue(throwError(() => new Error('nope')))
    };
    const criticalActionService = { create: jasmine.createSpy('create') };
    const snackBar = { open: jasmine.createSpy('open') };

    await configure(orderService, criticalActionService, snackBar);
    const fixture = TestBed.createComponent(OrderDetailComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.order).toBeNull();
    expect(component.loadError).toContain('could not be loaded');
  });

  it('submits a protected cancellation request with the current order id', async () => {
    const order = fullOrder(7);
    const orderService = { getOrder: jasmine.createSpy('getOrder').and.returnValue(of(order)) };
    const criticalActionService = {
      create: jasmine.createSpy('create').and.returnValue(of({ id: 99 }))
    };
    const snackBar = { open: jasmine.createSpy('open') };

    await configure(orderService, criticalActionService, snackBar, '7');
    const fixture = TestBed.createComponent(OrderDetailComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.requestProtectedCancellation();
    expect(criticalActionService.create).toHaveBeenCalled();
    const body = criticalActionService.create.calls.mostRecent().args[0];
    expect(body).toEqual(jasmine.objectContaining({
      requestType: 'ORDER_CANCELLATION_AFTER_APPROVAL',
      targetType: 'ORDER',
      targetId: 7
    }));
    expect(snackBar.open).toHaveBeenCalled();
  });

  it('does nothing when cancellation is requested without a loaded order', async () => {
    const orderService = { getOrder: jasmine.createSpy('getOrder').and.returnValue(throwError(() => new Error('x'))) };
    const criticalActionService = { create: jasmine.createSpy('create') };
    const snackBar = { open: jasmine.createSpy('open') };

    await configure(orderService, criticalActionService, snackBar);
    const fixture = TestBed.createComponent(OrderDetailComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.requestProtectedCancellation();
    expect(criticalActionService.create).not.toHaveBeenCalled();
  });
});
