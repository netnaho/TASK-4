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
});
