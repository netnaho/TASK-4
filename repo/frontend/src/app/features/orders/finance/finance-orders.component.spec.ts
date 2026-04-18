import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { FinanceOrdersComponent } from './finance-orders.component';
import { OrderService } from '../../../core/services/order.service';

function approvedOrder(id: number) {
  return {
    id,
    orderNumber: `ORD-${id}`,
    status: 'APPROVED',
    buyer: 'Buyer One',
    createdAt: '',
    totalOrderedQuantity: 4,
    totalShippedQuantity: 0,
    totalReceivedQuantity: 0,
    discrepancyOpen: false
  };
}

describe('FinanceOrdersComponent', () => {
  it('loads approved orders and records payment with form values', async () => {
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({
        content: [approvedOrder(1)],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
        first: true,
        last: true
      })),
      recordPayment: jasmine.createSpy('recordPayment').and.returnValue(of({}))
    };
    const snackBar = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [FinanceOrdersComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(FinanceOrdersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(orderService.listOrders).toHaveBeenCalledWith({ status: 'APPROVED' });
    expect(component.orders.length).toBe(1);

    component.form.setValue({ referenceNumber: 'PAY-42', amount: 250 });
    component.record(1);

    expect(orderService.recordPayment).toHaveBeenCalledWith(1, { referenceNumber: 'PAY-42', amount: 250 });
    expect(orderService.listOrders).toHaveBeenCalledTimes(2);
    expect(component.loadError).toBe('');
  });

  it('reports a graceful error when approved orders cannot be loaded', async () => {
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(throwError(() => new Error('boom'))),
      recordPayment: jasmine.createSpy('recordPayment')
    };
    const snackBar = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [FinanceOrdersComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(FinanceOrdersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.orders.length).toBe(0);
    expect(component.loadError).toContain('could not be loaded');
  });

  it('surfaces a snack-bar error when payment recording fails', async () => {
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({
        content: [approvedOrder(9)],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
        first: true,
        last: true
      })),
      recordPayment: jasmine.createSpy('recordPayment').and.returnValue(throwError(() => new Error('rejected')))
    };
    const snackBar = { open: jasmine.createSpy('open') };

    await TestBed.configureTestingModule({
      imports: [FinanceOrdersComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(FinanceOrdersComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    component.record(9);

    expect(snackBar.open).toHaveBeenCalled();
  });
});
