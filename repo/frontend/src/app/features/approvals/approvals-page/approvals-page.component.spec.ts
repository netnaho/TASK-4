import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, Subject, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApprovalsPageComponent } from './approvals-page.component';
import { CriticalActionService } from '../../../core/services/critical-action.service';
import { AuthService } from '../../../core/services/auth.service';

function request(status = 'PENDING') {
  return {
    id: 7,
    requestType: 'ORDER_CANCELLATION_AFTER_APPROVAL',
    targetType: 'ORDER',
    targetId: 99,
    justification: 'Dual approval required',
    requestedBy: 'Buyer One',
    status,
    createdAt: new Date().toISOString(),
    expiresAt: new Date().toISOString(),
    approvalCount: 0,
    approvals: [],
    auditEvents: []
  };
}

describe('ApprovalsPageComponent failure state', () => {
  it('shows error fallback when critical action list fails', async () => {
    const criticalActionService = {
      list: jasmine.createSpy('list').and.returnValue(throwError(() => ({ status: 403 }))),
      get: jasmine.createSpy('get'),
      decide: jasmine.createSpy('decide')
    };

    await TestBed.configureTestingModule({
      imports: [ApprovalsPageComponent, NoopAnimationsModule],
      providers: [
        { provide: CriticalActionService, useValue: criticalActionService },
        { provide: AuthService, useValue: { userSnapshot: () => ({ role: 'QUALITY_REVIEWER' }) } },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ApprovalsPageComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Critical action queue could not be loaded right now.');
  });

  it('hides decision controls for non-approver viewers', async () => {
    const criticalActionService = {
      list: jasmine.createSpy('list').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      get: jasmine.createSpy('get'),
      decide: jasmine.createSpy('decide')
    };

    await TestBed.configureTestingModule({
      imports: [ApprovalsPageComponent, NoopAnimationsModule],
      providers: [
        { provide: CriticalActionService, useValue: criticalActionService },
        { provide: AuthService, useValue: { userSnapshot: () => ({ role: 'BUYER' }) } },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ApprovalsPageComponent);
    fixture.componentInstance.selectedRequest = request();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('This request is visible for audit only.');
    const buttons = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button')).map((button) => button.textContent?.trim());

    expect(buttons).not.toContain('Approve');
  });

  it('ignores duplicate approval clicks while a decision is in flight', async () => {
    const decision$ = new Subject<ReturnType<typeof request>>();
    const criticalActionService = {
      list: jasmine.createSpy('list').and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true })),
      get: jasmine.createSpy('get'),
      decide: jasmine.createSpy('decide').and.returnValue(decision$)
    };

    await TestBed.configureTestingModule({
      imports: [ApprovalsPageComponent, NoopAnimationsModule],
      providers: [
        { provide: CriticalActionService, useValue: criticalActionService },
        { provide: AuthService, useValue: { userSnapshot: () => ({ role: 'FINANCE' }) } },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ApprovalsPageComponent);
    fixture.componentInstance.selectedRequest = request();
    fixture.detectChanges();

    fixture.componentInstance.approve(7);
    fixture.componentInstance.approve(7);

    expect(criticalActionService.decide).toHaveBeenCalledTimes(1);
  });
});
