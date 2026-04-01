import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, of } from 'rxjs';
import { CriticalActionService } from '../../../core/services/critical-action.service';
import { CriticalActionRequestModel } from '../../../core/models/critical-action.models';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-approvals-page',
  standalone: true,
  imports: [CommonModule, DatePipe, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './approvals-page.component.html',
  styleUrl: './approvals-page.component.scss'
})
export class ApprovalsPageComponent implements OnInit {
  readonly userRole = this.authService.userSnapshot()?.role ?? null;
  readonly canDecide = ['QUALITY_REVIEWER', 'FINANCE', 'SYSTEM_ADMINISTRATOR'].includes(this.userRole ?? '');

  requests: CriticalActionRequestModel[] = [];
  selectedRequest: CriticalActionRequestModel | null = null;
  loadError = '';
  isDecisionSubmitting = false;
  readonly form = this.fb.group({ comments: ['Reviewed and approved through critical action queue.'] });

  constructor(
    private readonly fb: FormBuilder,
    private readonly criticalActionService: CriticalActionService,
    private readonly snackBar: MatSnackBar,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  open(id: number): void {
    this.criticalActionService.get(id).subscribe({
      next: (request) => {
        this.selectedRequest = request;
      },
      error: () => this.snackBar.open('Request details could not be loaded.', 'Dismiss', { duration: 2600 })
    });
  }

  approve(id: number): void {
    if (!this.canDecide || this.isDecisionSubmitting) {
      return;
    }
    this.isDecisionSubmitting = true;
    this.criticalActionService.decide(id, { decision: 'APPROVE', comments: this.form.value.comments }).subscribe({
      next: (request) => {
        this.selectedRequest = request;
        this.snackBar.open('Critical action approval recorded', 'Dismiss', { duration: 2200 });
        this.reload();
      },
      error: () => this.snackBar.open('Approval failed. Please retry.', 'Dismiss', { duration: 2600 })
    }).add(() => {
      this.isDecisionSubmitting = false;
    });
  }

  reject(id: number): void {
    if (!this.canDecide || this.isDecisionSubmitting) {
      return;
    }
    this.isDecisionSubmitting = true;
    this.criticalActionService.decide(id, { decision: 'REJECT', comments: 'Rejected in approval queue.' }).subscribe({
      next: (request) => {
        this.selectedRequest = request;
        this.snackBar.open('Critical action rejected', 'Dismiss', { duration: 2200 });
        this.reload();
      },
      error: () => this.snackBar.open('Rejection failed. Please retry.', 'Dismiss', { duration: 2600 })
    }).add(() => {
      this.isDecisionSubmitting = false;
    });
  }

  private reload(): void {
    this.loadError = '';
    this.criticalActionService.list().pipe(
      catchError(() => {
        this.requests = [];
        this.selectedRequest = null;
        this.loadError = 'Critical action queue could not be loaded right now.';
        return of({ content: [] as CriticalActionRequestModel[], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true });
      })
    ).subscribe({ next: (page) => (this.requests = page.content) });
  }
}
