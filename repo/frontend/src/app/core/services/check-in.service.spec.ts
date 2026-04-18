import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CheckInService } from './check-in.service';
import { environment } from '../../../environments/environment';

describe('CheckInService', () => {
  let service: CheckInService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CheckInService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CheckInService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list() forwards pagination params', () => {
    service.list({ page: 1, size: 10, sort: 'updatedAt,desc' }).subscribe();
    const req = http.expectOne((r) => r.url === '/check-ins');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('updatedAt,desc');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true });
  });

  it('get() calls GET /check-ins/:id', () => {
    service.get(2).subscribe();
    http.expectOne('/check-ins/2').flush({});
  });

  it('create() posts multipart form with all attached files', () => {
    const files = [
      new File([new Uint8Array([1])], 'a.png', { type: 'image/png' }),
      new File([new Uint8Array([2])], 'b.wav', { type: 'audio/wav' })
    ];
    service.create('{}', files).subscribe();
    const req = http.expectOne('/check-ins');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({});
  });

  it('update() uses PUT /check-ins/:id', () => {
    service.update(7, '{}', []).subscribe();
    const req = http.expectOne('/check-ins/7');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('attachmentUrl() builds absolute download URL', () => {
    const root = environment.apiBaseUrl.endsWith('/') ? environment.apiBaseUrl.slice(0, -1) : environment.apiBaseUrl;
    expect(service.attachmentUrl(3, 9)).toBe(`${root}/check-ins/3/attachments/9/download`);
  });
});
