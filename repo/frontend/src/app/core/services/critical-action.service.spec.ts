import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { CriticalActionService } from './critical-action.service';
import { ApiService } from './api.service';

describe('CriticalActionService', () => {
  let apiService: { get: jasmine.Spy; post: jasmine.Spy; put: jasmine.Spy };
  let service: CriticalActionService;

  beforeEach(() => {
    apiService = {
      get: jasmine.createSpy('get').and.returnValue(of({})),
      post: jasmine.createSpy('post').and.returnValue(of({})),
      put: jasmine.createSpy('put').and.returnValue(of({}))
    };
    TestBed.configureTestingModule({
      providers: [CriticalActionService, { provide: ApiService, useValue: apiService }]
    });
    service = TestBed.inject(CriticalActionService);
  });

  it('list() forwards pagination and status filter', () => {
    service.list({ page: 1, size: 5, sort: 'updatedAt,desc', status: 'PENDING' }).subscribe();
    const args = apiService.get.calls.mostRecent().args as [string, any];
    expect(args[0]).toBe('/critical-actions');
    expect(args[1].params.get('status')).toBe('PENDING');
    expect(args[1].params.get('page')).toBe('1');
    expect(args[1].params.get('size')).toBe('5');
    expect(args[1].params.get('sort')).toBe('updatedAt,desc');
  });

  it('get(), create(), decide() target the expected paths', () => {
    service.get(7).subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/critical-actions/7');

    service.create({ requestType: 'ORDER_CANCELLATION_AFTER_APPROVAL' }).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/critical-actions');

    service.decide(5, { decision: 'APPROVE' }).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/critical-actions/5/decision');
  });
});
