import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { ReturnsComponent } from './returns.component';
import { OrderService } from '../../../core/services/order.service';

describe('ReturnsComponent', () => {
  it('loads admin-managed return and after-sales reason codes', async () => {
    const orderService = {
      listOrders: jasmine.createSpy('listOrders').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      listReasonCodes: jasmine.createSpy('listReasonCodes').and.callFake((codeType: 'RETURN' | 'AFTER_SALES') => of(codeType === 'RETURN'
        ? [{ codeType: 'RETURN', code: 'RETURN_ONLY', label: 'Return Only' }]
        : [{ codeType: 'AFTER_SALES', code: 'CASE_ONLY', label: 'Case Only' }])),
      getOrder: jasmine.createSpy('getOrder'),
      createReturn: jasmine.createSpy('createReturn'),
      createAfterSalesCase: jasmine.createSpy('createAfterSalesCase')
    };

    await TestBed.configureTestingModule({
      imports: [ReturnsComponent, NoopAnimationsModule],
      providers: [{ provide: OrderService, useValue: orderService }]
    }).compileComponents();

    const fixture = TestBed.createComponent(ReturnsComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.returnReasonCodes[0].code).toBe('RETURN_ONLY');
    expect(component.afterSalesReasonCodes[0].code).toBe('CASE_ONLY');
    expect(component.form.value.returnReasonCode).toBe('RETURN_ONLY');
    expect(component.form.value.afterSalesReasonCode).toBe('CASE_ONLY');
  });
});
