import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  const navigate = jasmine.createSpy('navigate');
  const authService = {
    ensureInitialized: jasmine.createSpy('ensureInitialized')
  };

  beforeEach(() => {
    navigate.calls.reset();
    authService.ensureInitialized.calls.reset();
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: { navigate } },
        { provide: AuthService, useValue: authService }
      ]
    });
  });

  it('allows navigation when role is authorized', (done) => {
    authService.ensureInitialized.and.returnValue(of({ id: 2, username: 'quality1', displayName: 'Quality', role: 'QUALITY_REVIEWER', permissions: [] }));
    const route = { data: { roles: ['QUALITY_REVIEWER', 'SYSTEM_ADMINISTRATOR'] } } as never;
    const result$ = TestBed.runInInjectionContext(() => roleGuard(route, {} as never)) as Observable<boolean>;
    result$.subscribe((allowed) => {
      expect(allowed).toBeTrue();
      expect(navigate).not.toHaveBeenCalled();
      done();
    });
  });

  it('redirects to unauthorized when role is not allowed', (done) => {
    authService.ensureInitialized.and.returnValue(of({ id: 3, username: 'buyer1', displayName: 'Buyer', role: 'BUYER', permissions: [] }));
    const route = { data: { roles: ['QUALITY_REVIEWER'] } } as never;
    const result$ = TestBed.runInInjectionContext(() => roleGuard(route, {} as never)) as Observable<boolean>;
    result$.subscribe((allowed) => {
      expect(allowed).toBeFalse();
      expect(navigate).toHaveBeenCalledWith(['/unauthorized']);
      done();
    });
  });
});
