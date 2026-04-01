import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
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

  it('allows navigation when authenticated', (done) => {
    authService.ensureInitialized.and.returnValue(of({ id: 1, username: 'buyer1', displayName: 'Buyer', role: 'BUYER', permissions: [] }));
    const result$ = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never)) as Observable<boolean>;
    result$.subscribe((allowed) => {
      expect(allowed).toBeTrue();
      expect(navigate).not.toHaveBeenCalled();
      done();
    });
  });

  it('redirects to login when unauthenticated', (done) => {
    authService.ensureInitialized.and.returnValue(of(null));
    const result$ = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never)) as Observable<boolean>;
    result$.subscribe((allowed) => {
      expect(allowed).toBeFalse();
      expect(navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
  });
});
