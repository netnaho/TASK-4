import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Subject, of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DocumentCenterComponent } from './document-center.component';
import { DocumentCenterService } from '../../../core/services/document-center.service';
import { AuthService } from '../../../core/services/auth.service';
import { CriticalActionService } from '../../../core/services/critical-action.service';

describe('DocumentCenterComponent', () => {
  it('prevents duplicate draft creation while the request is in flight', async () => {
    const createDraft$ = new Subject<any>();
    const documentCenterService = {
      listTypes: jasmine.createSpy('listTypes').and.returnValue(of([{ id: 1, code: 'SOP', name: 'SOP', description: '', evidenceAllowed: false, active: true }])),
      listTemplates: jasmine.createSpy('listTemplates').and.returnValue(of([])),
      listDocuments: jasmine.createSpy('listDocuments').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      approvalQueue: jasmine.createSpy('approvalQueue').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      archiveList: jasmine.createSpy('archiveList').and.returnValue(of([])),
      createDraft: jasmine.createSpy('createDraft').and.returnValue(createDraft$),
      createTemplate: jasmine.createSpy('createTemplate'),
      getDocument: jasmine.createSpy('getDocument'),
      submitForApproval: jasmine.createSpy('submitForApproval'),
      approve: jasmine.createSpy('approve'),
      archive: jasmine.createSpy('archive'),
      preview: jasmine.createSpy('preview'),
      fetchContentBlob: jasmine.createSpy('fetchContentBlob'),
      downloadUrl: jasmine.createSpy('downloadUrl').and.returnValue('/api/documents/1/download')
    };

    await TestBed.configureTestingModule({
      imports: [DocumentCenterComponent, NoopAnimationsModule],
      providers: [
        { provide: DocumentCenterService, useValue: documentCenterService },
        { provide: AuthService, useValue: { userSnapshot: () => ({ role: 'BUYER' }) } },
        { provide: CriticalActionService, useValue: { create: jasmine.createSpy('create') } },
        { provide: MatDialog, useValue: { open: jasmine.createSpy('open').and.returnValue({ afterClosed: () => of(null) }) } },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DocumentCenterComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.draftForm.patchValue({ documentTypeId: 1, title: 'Controlled Draft', approvalRoles: ['QUALITY_REVIEWER'] });
    component.createDraft();
    component.createDraft();

    expect(documentCenterService.createDraft).toHaveBeenCalledTimes(1);
    expect(component.isActionPending(component.draftActionKey())).toBeTrue();
  });
});
