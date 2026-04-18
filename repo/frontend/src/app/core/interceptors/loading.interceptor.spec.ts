import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { loadingInterceptor } from './loading.interceptor';
import { LoadingService } from '../services/loading.service';

describe('loadingInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let loading: { begin: jasmine.Spy; end: jasmine.Spy };

  beforeEach(() => {
    loading = {
      begin: jasmine.createSpy('begin'),
      end: jasmine.createSpy('end')
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: LoadingService, useValue: loading },
        provideHttpClient(withInterceptors([loadingInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('increments loading counter before the request and decrements after completion', () => {
    http.get('/anything').subscribe();
    const req = controller.expectOne('/anything');
    expect(loading.begin).toHaveBeenCalledTimes(1);
    expect(loading.end).not.toHaveBeenCalled();
    req.flush({});
    expect(loading.end).toHaveBeenCalledTimes(1);
  });

  it('still ends loading when request fails', () => {
    http.get('/fail').subscribe({ next: () => undefined, error: () => undefined });
    const req = controller.expectOne('/fail');
    req.error(new ProgressEvent('error'));
    expect(loading.end).toHaveBeenCalledTimes(1);
  });
});
