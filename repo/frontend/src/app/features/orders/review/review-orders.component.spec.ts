import { TestBed } from '@angular/core/testing';
import { throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReviewOrdersComponent } from './review-orders.component';
import { OrderService } from '../../../core/services/order.service';

describe('ReviewOrdersComponent failure state', () => {
  it('shows queue error message when list call fails with 403', async () => {
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(throwError(() => ({ status: 403 }))),
      approve: jasmine.createSpy('approve')
    };

    await TestBed.configureTestingModule({
      imports: [ReviewOrdersComponent],
      providers: [
        { provide: OrderService, useValue: orderService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ReviewOrdersComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Review queue could not be loaded right now.');
  });
});
