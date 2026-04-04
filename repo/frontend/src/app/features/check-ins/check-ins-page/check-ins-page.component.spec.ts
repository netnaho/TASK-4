import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Subject, of } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CheckInsPageComponent } from './check-ins-page.component';
import { CheckInService } from '../../../core/services/check-in.service';

describe('CheckInsPageComponent', () => {
  it('prevents duplicate quick-create requests while a check-in is being created', async () => {
    const create$ = new Subject<any>();
    const checkInService = {
      list: jasmine.createSpy('list').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      create: jasmine.createSpy('create').and.returnValue(create$),
      update: jasmine.createSpy('update'),
      get: jasmine.createSpy('get'),
      attachmentUrl: jasmine.createSpy('attachmentUrl').and.returnValue('/api/check-ins/1/attachments/1/download')
    };

    await TestBed.configureTestingModule({
      imports: [CheckInsPageComponent, NoopAnimationsModule],
      providers: [
        { provide: CheckInService, useValue: checkInService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CheckInsPageComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.form.patchValue({ commentText: 'Dock inspection complete' });
    component.quickCreate();
    component.quickCreate();

    expect(checkInService.create).toHaveBeenCalledTimes(1);
    expect(component.isCreating).toBeTrue();
  });

  it('submits reported device time and optional coordinates in the payload', async () => {
    const checkInService = {
      list: jasmine.createSpy('list').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      create: jasmine.createSpy('create').and.returnValue(of({ id: 1, owner: 'Buyer One', commentText: 'Dock inspection complete', deviceTimestamp: '2026-04-04T10:05:00.000Z', serverReceivedAt: '2026-04-04T10:05:02.000Z', attachments: [], revisions: [], auditEvents: [], createdAt: '', updatedAt: '' })),
      update: jasmine.createSpy('update'),
      get: jasmine.createSpy('get'),
      attachmentUrl: jasmine.createSpy('attachmentUrl').and.returnValue('/api/check-ins/1/attachments/1/download')
    };

    await TestBed.configureTestingModule({
      imports: [CheckInsPageComponent, NoopAnimationsModule],
      providers: [
        { provide: CheckInService, useValue: checkInService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CheckInsPageComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.form.patchValue({
      commentText: 'Dock inspection complete',
      deviceTimestamp: '2026-04-04T10:05:00.000Z',
      latitude: 9.01,
      longitude: 38.76
    });
    component.quickCreate();

    const payload = JSON.parse(checkInService.create.calls.mostRecent().args[0] as string);
    expect(payload.deviceTimestamp).toBeTruthy();
    expect(payload.latitude).toBe(9.01);
    expect(payload.longitude).toBe(38.76);
  });

  it('highlights device time and coordinate revision fields', async () => {
    const checkInService = {
      list: jasmine.createSpy('list').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      create: jasmine.createSpy('create'),
      update: jasmine.createSpy('update'),
      get: jasmine.createSpy('get'),
      attachmentUrl: jasmine.createSpy('attachmentUrl').and.returnValue('/api/check-ins/1/attachments/1/download')
    };

    await TestBed.configureTestingModule({
      imports: [CheckInsPageComponent, NoopAnimationsModule],
      providers: [
        { provide: CheckInService, useValue: checkInService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CheckInsPageComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const revision = { id: 1, revisionNumber: 2, commentText: 'Updated', deviceTimestamp: '2026-04-04T10:05:00.000Z', latitude: 9.01, longitude: 38.76, changedFields: ['deviceTimestamp', 'latitude'], attachments: [], editedBy: 'Buyer One', createdAt: '2026-04-04T10:05:02.000Z' };
    expect(component.highlightClass(revision, 'deviceTimestamp')).toBe('changed');
    expect(component.highlightClass(revision, 'latitude')).toBe('changed');
  });
});
