import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { errorInterceptor } from './error.interceptor';
import { SKIP_ERROR_TOAST } from './http-context.tokens';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let snackBar: { open: jasmine.Spy };

  beforeEach(() => {
    snackBar = { open: jasmine.createSpy('open') };
    TestBed.configureTestingModule({
      providers: [
        { provide: MatSnackBar, useValue: snackBar },
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('shows a snack-bar with backend message when request fails', () => {
    http.get('/oops').subscribe({ next: () => undefined, error: () => undefined });
    const req = controller.expectOne('/oops');
    req.flush({ message: 'Custom error' }, { status: 500, statusText: 'err' });

    expect(snackBar.open).toHaveBeenCalled();
    const firstArg = snackBar.open.calls.mostRecent().args[0];
    expect(firstArg).toBe('Custom error');
  });

  it('falls back to a generic message when none is provided', () => {
    http.get('/oops').subscribe({ next: () => undefined, error: () => undefined });
    const req = controller.expectOne('/oops');
    req.flush(null, { status: 500, statusText: 'err' });

    expect(snackBar.open).toHaveBeenCalled();
    const firstArg = snackBar.open.calls.mostRecent().args[0];
    expect(firstArg).toContain('Service is temporarily unavailable');
  });

  it('does not toast when SKIP_ERROR_TOAST context is set', () => {
    const ctx = new HttpContext().set(SKIP_ERROR_TOAST, true);
    http.get('/silent', { context: ctx }).subscribe({ next: () => undefined, error: () => undefined });
    const req = controller.expectOne('/silent');
    req.flush({ message: 'should not toast' }, { status: 500, statusText: 'err' });

    expect(snackBar.open).not.toHaveBeenCalled();
  });
});
