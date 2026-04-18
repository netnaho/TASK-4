import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { ApiService } from './api.service';
import { CsrfService } from './csrf.service';

describe('AuthService', () => {
  let apiService: { get: jasmine.Spy; post: jasmine.Spy; put: jasmine.Spy };
  let csrfService: CsrfService;

  beforeEach(() => {
    apiService = {
      get: jasmine.createSpy('get'),
      post: jasmine.createSpy('post'),
      put: jasmine.createSpy('put')
    };
    csrfService = new CsrfService();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: ApiService, useValue: apiService },
        { provide: CsrfService, useValue: csrfService }
      ]
    });
  });

  it('stores CSRF token on fetchCsrfToken()', (done) => {
    apiService.get.and.returnValue(of({ token: 'abc-123' }));
    const service = TestBed.inject(AuthService);
    service.fetchCsrfToken().subscribe(() => {
      expect(csrfService.getToken()).toBe('abc-123');
      done();
    });
  });

  it('publishes the authenticated user after login', (done) => {
    apiService.post.and.returnValue(of({ user: { id: 1, username: 'buyer1', displayName: 'Buyer', role: 'BUYER', permissions: [] } }));
    const service = TestBed.inject(AuthService);
    service.login({ username: 'buyer1', password: 'x' } as any).subscribe(() => {
      expect(service.userSnapshot()).toEqual(jasmine.objectContaining({ username: 'buyer1', role: 'BUYER' }));
      expect(service.hasAnyRole(['BUYER'])).toBeTrue();
      expect(service.hasAnyRole(['FINANCE'])).toBeFalse();
      done();
    });
  });

  it('clears the user on logout', (done) => {
    apiService.post.and.returnValue(of({}));
    apiService.get.and.returnValue(of({ token: 't' }));
    const service = TestBed.inject(AuthService);
    service.login({ username: 'u', password: 'p' } as any).subscribe(() => {
      apiService.post.and.returnValue(of({}));
      service.logout().subscribe(() => {
        expect(service.userSnapshot()).toBeNull();
        done();
      });
    });
    // Prime login
    apiService.post.and.returnValue(of({ user: { id: 1, username: 'u', displayName: 'U', role: 'BUYER', permissions: [] } }));
    service.login({ username: 'u', password: 'p' } as any).subscribe();
  });

  it('extractApiError returns inner error when present', () => {
    const service = TestBed.inject(AuthService);
    const result = service.extractApiError({ error: { message: 'bad' } });
    expect(result).toEqual({ message: 'bad' } as any);
    expect(service.extractApiError(null)).toBeNull();
  });

  it('me() resets state to null on 401', (done) => {
    apiService.get.and.returnValue(throwError(() => ({ status: 401 })));
    const service = TestBed.inject(AuthService);
    service.me().subscribe((u) => {
      expect(u).toBeNull();
      expect(service.userSnapshot()).toBeNull();
      done();
    });
  });

  it('getCaptcha() URL-encodes the username', () => {
    apiService.get.and.returnValue(of({ challengeId: 'c', question: '1 + 2' }));
    const service = TestBed.inject(AuthService);
    service.getCaptcha('user with space').subscribe();
    expect(apiService.get).toHaveBeenCalled();
    const args = apiService.get.calls.mostRecent().args as [string, any];
    expect(args[0]).toContain('/auth/captcha?username=user%20with%20space');
  });
});
