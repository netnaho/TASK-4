import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { Subject, of } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrderWorkspaceComponent } from './order-workspace.component';
import { OrderService } from '../../../core/services/order.service';

describe('OrderWorkspaceComponent', () => {
  it('prevents duplicate draft submissions while create order is in flight', async () => {
    const createOrder$ = new Subject<any>();
    const orderService = {
      listCatalog: jasmine.createSpy('listCatalog').and.returnValue(of([{ id: 1, sku: 'SKU-1', name: 'Item 1', unitPrice: 12, unit: 'box' }])),
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      createOrder: jasmine.createSpy('createOrder').and.returnValue(createOrder$)
    };

    await TestBed.configureTestingModule({
      imports: [OrderWorkspaceComponent, NoopAnimationsModule],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(OrderWorkspaceComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.items.at(0).patchValue({ productId: 1, quantity: 2 });
    component.submit();
    component.submit();

    expect(orderService.createOrder).toHaveBeenCalledTimes(1);
    expect(component.isCreatingDraft).toBeTrue();
  });
});
