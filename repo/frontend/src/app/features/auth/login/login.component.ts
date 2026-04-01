import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { CaptchaChallengeModel } from '../../../core/models/auth.models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  hidePassword = true;
  isSubmitting = false;
  loginError = '';
  lockoutMessage = '';
  captchaChallenge: CaptchaChallengeModel | null = null;

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
    captchaAnswer: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private readonly authService: AuthService
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.loginError = '';
    this.lockoutMessage = '';

    this.authService.login({
      username: this.form.getRawValue().username,
      password: this.form.getRawValue().password,
      captchaChallengeId: this.captchaChallenge?.challengeId,
      captchaAnswer: this.form.getRawValue().captchaAnswer || null
    }).pipe(finalize(() => (this.isSubmitting = false))).subscribe({
      next: () => {
        this.snackBar.open('Welcome to PharmaProcure', 'Dismiss', { duration: 2500 });
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        const apiError = this.authService.extractApiError(error);
        const details = apiError?.details ?? [];
        this.loginError = apiError?.message ?? 'Unable to sign in';

        if (apiError?.code === 423) {
          this.lockoutMessage = details.find((detail) => detail.startsWith('LOCKED_UNTIL:')) ?? '';
        }

        if (details.includes('CAPTCHA_REQUIRED')) {
          this.loadCaptcha();
        }
      }
    });
  }

  private loadCaptcha(): void {
    const username = this.form.getRawValue().username;
    if (!username) {
      return;
    }

    this.authService.getCaptcha(username).subscribe({
      next: (challenge) => {
        this.captchaChallenge = challenge.required ? challenge : null;
      }
    });
  }
}
