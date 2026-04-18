import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AdminService } from './admin.service';
import { ApiService } from './api.service';

describe('AdminService', () => {
  let apiService: { get: jasmine.Spy; post: jasmine.Spy; put: jasmine.Spy };
  let service: AdminService;

  beforeEach(() => {
    apiService = {
      get: jasmine.createSpy('get').and.returnValue(of({})),
      post: jasmine.createSpy('post').and.returnValue(of({})),
      put: jasmine.createSpy('put').and.returnValue(of({}))
    };
    TestBed.configureTestingModule({
      providers: [AdminService, { provide: ApiService, useValue: apiService }]
    });
    service = TestBed.inject(AdminService);
  });

  it('routes GET lookups to the correct admin sub-paths', () => {
    service.users().subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/admin/users');

    service.permissions().subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/admin/permissions');

    service.stateMachine().subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/admin/state-machine');

    service.documentTypes().subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/admin/document-types');

    service.reasonCodes().subscribe();
    expect(apiService.get.calls.mostRecent().args[0]).toBe('/admin/reason-codes');
  });

  it('PUT/POST helpers address the correct path and method', () => {
    service.updateUserAccess(4, { active: false }).subscribe();
    expect(apiService.put.calls.mostRecent().args[0]).toBe('/admin/users/4');

    service.updateStateMachineTransition(9, { active: true }).subscribe();
    expect(apiService.put.calls.mostRecent().args[0]).toBe('/admin/state-machine/9');

    service.updateDocumentType(2, { description: 'x', evidenceAllowed: true, active: true }).subscribe();
    expect(apiService.put.calls.mostRecent().args[0]).toBe('/admin/document-types/2');

    service.createReasonCode({ codeType: 'RETURN', code: 'X', label: 'Y', active: true }).subscribe();
    expect(apiService.post.calls.mostRecent().args[0]).toBe('/admin/reason-codes');

    service.updateReasonCode(7, { label: 'New', active: false }).subscribe();
    expect(apiService.put.calls.mostRecent().args[0]).toBe('/admin/reason-codes/7');
  });
});
