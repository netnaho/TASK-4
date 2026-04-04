import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminPageComponent } from './admin-page.component';
import { AdminService } from '../../../core/services/admin.service';

describe('AdminPageComponent', () => {
  it('saves the selected state machine transition', async () => {
    const adminService = {
      users: jasmine.createSpy('users').and.returnValue(of([])),
      updateUserAccess: jasmine.createSpy('updateUserAccess'),
      permissions: jasmine.createSpy('permissions').and.returnValue(of([])),
      stateMachine: jasmine.createSpy('stateMachine').and.returnValue(of({ transitions: [{ id: 7, fromStatus: 'PICK_PACK', toStatus: 'SHIPPED', active: true }] })),
      updateStateMachineTransition: jasmine.createSpy('updateStateMachineTransition').and.returnValue(of({ id: 7, fromStatus: 'PICK_PACK', toStatus: 'SHIPPED', active: false })),
      documentTypes: jasmine.createSpy('documentTypes').and.returnValue(of([])),
      updateDocumentType: jasmine.createSpy('updateDocumentType'),
      reasonCodes: jasmine.createSpy('reasonCodes').and.returnValue(of([])),
      createReasonCode: jasmine.createSpy('createReasonCode'),
      updateReasonCode: jasmine.createSpy('updateReasonCode')
    };

    await TestBed.configureTestingModule({
      imports: [AdminPageComponent, NoopAnimationsModule],
      providers: [
        { provide: AdminService, useValue: adminService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(AdminPageComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.selectTransition({ id: 7, fromStatus: 'PICK_PACK', toStatus: 'SHIPPED', active: true });
    component.transitionForm.patchValue({ active: false });
    component.saveTransition();

    expect(adminService.updateStateMachineTransition).toHaveBeenCalledWith(7, { active: false });
  });

  it('updates selected user access state', async () => {
    const adminService = {
      users: jasmine.createSpy('users').and.returnValue(of([{ id: 3, username: 'buyer1', displayName: 'Buyer One', role: 'BUYER', active: true }])),
      updateUserAccess: jasmine.createSpy('updateUserAccess').and.returnValue(of({ id: 3, username: 'buyer1', displayName: 'Buyer One', role: 'BUYER', active: false })),
      permissions: jasmine.createSpy('permissions').and.returnValue(of([])),
      stateMachine: jasmine.createSpy('stateMachine').and.returnValue(of({ transitions: [] })),
      updateStateMachineTransition: jasmine.createSpy('updateStateMachineTransition'),
      documentTypes: jasmine.createSpy('documentTypes').and.returnValue(of([])),
      updateDocumentType: jasmine.createSpy('updateDocumentType'),
      reasonCodes: jasmine.createSpy('reasonCodes').and.returnValue(of([])),
      createReasonCode: jasmine.createSpy('createReasonCode'),
      updateReasonCode: jasmine.createSpy('updateReasonCode')
    };

    await TestBed.configureTestingModule({
      imports: [AdminPageComponent, NoopAnimationsModule],
      providers: [
        { provide: AdminService, useValue: adminService },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(AdminPageComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.selectUser({ id: 3, username: 'buyer1', displayName: 'Buyer One', role: 'BUYER', active: true });
    component.userAccessForm.patchValue({ active: false });
    component.saveUserAccess();

    expect(adminService.updateUserAccess).toHaveBeenCalledWith(3, { active: false });
  });
});
