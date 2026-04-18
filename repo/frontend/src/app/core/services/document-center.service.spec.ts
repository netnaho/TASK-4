import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DocumentCenterService } from './document-center.service';
import { environment } from '../../../environments/environment';

describe('DocumentCenterService', () => {
  let service: DocumentCenterService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DocumentCenterService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DocumentCenterService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('listTypes() calls GET /documents/types', () => {
    service.listTypes().subscribe((v) => expect(v).toEqual([] as any));
    const req = http.expectOne('/documents/types');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listTemplates() calls GET /documents/templates', () => {
    service.listTemplates().subscribe();
    http.expectOne('/documents/templates').flush([]);
  });

  it('listDocuments() forwards status + documentTypeId + pagination', () => {
    service.listDocuments({ page: 0, size: 10, sort: 's', status: 'DRAFT', documentTypeId: 2 }).subscribe();
    const req = http.expectOne((r) => r.url === '/documents');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('DRAFT');
    expect(req.request.params.get('documentTypeId')).toBe('2');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true });
  });

  it('approvalQueue() calls GET /documents/approval-queue', () => {
    service.approvalQueue().subscribe();
    http.expectOne('/documents/approval-queue').flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true });
  });

  it('archiveList() calls GET /documents/archive', () => {
    service.archiveList().subscribe();
    http.expectOne('/documents/archive').flush([]);
  });

  it('getDocument() calls GET /documents/:id', () => {
    service.getDocument(5).subscribe();
    http.expectOne('/documents/5').flush({});
  });

  it('createDraft() posts multipart form data', () => {
    const file = new File([new Uint8Array([1, 2, 3])], 'x.pdf', { type: 'application/pdf' });
    service.createDraft('{"a":1}', file).subscribe();
    const req = http.expectOne('/documents');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({});
  });

  it('updateDraft() uses PUT /documents/:id', () => {
    service.updateDraft(8, '{"a":2}').subscribe();
    const req = http.expectOne('/documents/8');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('submitForApproval / approve / archive POST to the correct sub-paths', () => {
    service.submitForApproval(1).subscribe();
    http.expectOne('/documents/1/submit-approval').flush({});

    service.approve(1, 'ok').subscribe();
    http.expectOne('/documents/1/approve').flush({});

    service.archive(1).subscribe();
    http.expectOne('/documents/1/archive').flush({});
  });

  it('contentUrl() / downloadUrl() produce absolute URLs rooted at apiBaseUrl', () => {
    const root = environment.apiBaseUrl.endsWith('/') ? environment.apiBaseUrl.slice(0, -1) : environment.apiBaseUrl;
    expect(service.contentUrl(3)).toBe(`${root}/documents/3/content`);
    expect(service.downloadUrl(3)).toBe(`${root}/documents/3/download`);
  });
});
