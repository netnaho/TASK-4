import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent failure states', () => {
  it('renders backend validation error message on failed login', async () => {
    const authService = {
      login: jasmine.createSpy('login').and.returnValue(
        throwError(() => ({ error: { code: 400, message: 'Validation failed', details: ['password: required'] } }))
      ),
      extractApiError: jasmine.createSpy('extractApiError').and.returnValue({ code: 400, message: 'Validation failed', details: ['password: required'] }),
      getCaptcha: jasmine.createSpy('getCaptcha').and.returnValue(of({ required: false }))
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, NoopAnimationsModule, MatIconTestingModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    component.form.patchValue({ username: 'buyer1', password: 'bad-password' });

    component.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Validation failed');
  });
});
