import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { sessionInterceptor } from './session.interceptor';
import { CsrfService } from '../services/csrf.service';

describe('sessionInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let csrf: CsrfService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([sessionInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    csrf = TestBed.inject(CsrfService);
  });

  afterEach(() => controller.verify());

  it('adds X-XSRF-TOKEN header on mutating requests when token is set', () => {
    csrf.setToken('tok-1');
    http.post('/do', {}).subscribe();
    const req = controller.expectOne('/do');
    expect(req.request.headers.get('X-XSRF-TOKEN')).toBe('tok-1');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({});
  });

  it('omits X-XSRF-TOKEN for safe requests', () => {
    csrf.setToken('tok-2');
    http.get('/read').subscribe();
    const req = controller.expectOne('/read');
    expect(req.request.headers.has('X-XSRF-TOKEN')).toBeFalse();
    req.flush({});
  });

  it('omits X-XSRF-TOKEN when no token is available', () => {
    http.post('/p', {}).subscribe();
    const req = controller.expectOne('/p');
    expect(req.request.headers.has('X-XSRF-TOKEN')).toBeFalse();
    req.flush({});
  });
});
