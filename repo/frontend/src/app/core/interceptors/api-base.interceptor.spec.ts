import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { apiBaseInterceptor } from './api-base.interceptor';
import { environment } from '../../../environments/environment';

describe('apiBaseInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiBaseInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('prefixes relative URLs with environment.apiBaseUrl', () => {
    http.get('/auth/me').subscribe();
    const req = controller.expectOne(`${environment.apiBaseUrl}/auth/me`);
    req.flush({});
  });

  it('adds a leading slash if missing', () => {
    http.get('auth/csrf').subscribe();
    const req = controller.expectOne(`${environment.apiBaseUrl}/auth/csrf`);
    req.flush({});
  });

  it('leaves absolute URLs unchanged', () => {
    http.get('http://external.example/api/x').subscribe();
    controller.expectOne('http://external.example/api/x').flush({});
  });
});
