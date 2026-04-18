import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { OrderService } from './order.service';
import { ApiService } from './api.service';

describe('OrderService', () => {
  let apiService: { get: jasmine.Spy; post: jasmine.Spy; put: jasmine.Spy };
  let service: OrderService;

  beforeEach(() => {
    apiService = {
      get: jasmine.createSpy('get').and.returnValue(of({})),
      post: jasmine.createSpy('post').and.returnValue(of({})),
      put: jasmine.createSpy('put').and.returnValue(of({}))
    };
    TestBed.configureTestingModule({
      providers: [OrderService, { provide: ApiService, useValue: apiService }]
    });
    service = TestBed.inject(OrderService);
  });

  it('GET /catalog/products is requested with a silent context', () => {
    service.listCatalog().subscribe();
    expect(apiService.get).toHaveBeenCalled();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/catalog/products');
  });

  it('listOrders() forwards page/size/sort/status parameters', () => {
    service.listOrders({ page: 2, size: 10, sort: 'createdAt,desc', status: 'APPROVED' }).subscribe();
    const args = apiService.get.calls.mostRecent().args as [string, any];
    expect(args[0]).toBe('/orders');
    const params = args[1].params as any;
    expect(params.get('page')).toBe('2');
    expect(params.get('size')).toBe('10');
    expect(params.get('sort')).toBe('createdAt,desc');
    expect(params.get('status')).toBe('APPROVED');
  });

  it('getOrder() targets the correct resource path', () => {
    service.getOrder(42).subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/orders/42');
  });

  it('listReasonCodes() sends codeType query param', () => {
    service.listReasonCodes('RETURN').subscribe();
    const args = apiService.get.calls.mostRecent().args as [string, any];
    expect(args[0]).toBe('/orders/reason-codes');
    expect(args[1].params.get('codeType')).toBe('RETURN');
  });

  it('lifecycle POST helpers target the correct sub-paths', () => {
    service.createOrder({}).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders');

    service.submitForReview(1).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/submit-review');

    service.cancelOrder(1).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/cancel');

    service.approve(1, { decision: 'APPROVED' }).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/approve');

    service.recordPayment(1, {}).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/record-payment');

    service.pickPack(1).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/pick-pack');

    service.createShipment(1, {}).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/shipments');

    service.createReceipt(1, {}).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/receipts');

    service.createReturn(1, {}).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/returns');

    service.createAfterSalesCase(1, {}).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/orders/1/after-sales-cases');
  });
});
