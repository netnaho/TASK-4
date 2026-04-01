import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, forkJoin, of } from 'rxjs';
import { AdminService } from '../../../core/services/admin.service';
import { AdminUserModel, PermissionOverviewModel, ReasonCodeModel, StateMachineConfigModel } from '../../../core/models/admin.models';
import { DocumentTypeModel } from '../../../core/models/document.models';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.scss'
})
export class AdminPageComponent implements OnInit {
  users: AdminUserModel[] = [];
  permissions: PermissionOverviewModel[] = [];
  stateMachine: StateMachineConfigModel | null = null;
  documentTypes: DocumentTypeModel[] = [];
  reasonCodes: ReasonCodeModel[] = [];
  selectedDocumentType: DocumentTypeModel | null = null;
  selectedReasonCode: ReasonCodeModel | null = null;
  loadError = '';

  readonly documentTypeForm = this.fb.group({ description: ['', Validators.required], evidenceAllowed: [false], active: [true] });

  readonly reasonCodeForm = this.fb.group({ codeType: ['RETURN', Validators.required], code: ['', Validators.required], label: ['', Validators.required], active: [true] });

  constructor(private readonly adminService: AdminService, private readonly fb: FormBuilder, private readonly snackBar: MatSnackBar) {}

  ngOnInit(): void { this.reload(); }

  createReasonCode(): void {
    if (this.reasonCodeForm.invalid) { this.reasonCodeForm.markAllAsTouched(); return; }
    this.adminService.createReasonCode(this.reasonCodeForm.getRawValue()).subscribe({ next: () => { this.snackBar.open('Reason code created', 'Dismiss', { duration: 2200 }); this.reload(); } });
  }

  selectDocumentType(type: DocumentTypeModel): void {
    this.selectedDocumentType = type;
    this.documentTypeForm.patchValue({ description: type.description ?? '', evidenceAllowed: type.evidenceAllowed, active: type.active });
  }

  saveDocumentType(): void {
    if (!this.selectedDocumentType) {
      return;
    }
    this.adminService.updateDocumentType(this.selectedDocumentType.id, this.documentTypeForm.getRawValue()).subscribe({ next: () => { this.snackBar.open('Document type updated', 'Dismiss', { duration: 2200 }); this.reload(); } });
  }

  selectReasonCode(reason: ReasonCodeModel): void {
    this.selectedReasonCode = reason;
    this.reasonCodeForm.patchValue({ codeType: reason.codeType, code: reason.code, label: reason.label, active: reason.active });
  }

  saveReasonCode(): void {
    if (!this.selectedReasonCode) {
      return;
    }
    this.adminService.updateReasonCode(this.selectedReasonCode.id, { label: this.reasonCodeForm.value.label, active: this.reasonCodeForm.value.active }).subscribe({ next: () => { this.snackBar.open('Reason code updated', 'Dismiss', { duration: 2200 }); this.reload(); } });
  }

  private reload(): void {
    this.loadError = '';
    forkJoin({ users: this.adminService.users(), permissions: this.adminService.permissions(), stateMachine: this.adminService.stateMachine(), documentTypes: this.adminService.documentTypes(), reasonCodes: this.adminService.reasonCodes() })
      .pipe(catchError(() => {
        this.users = [];
        this.permissions = [];
        this.stateMachine = null;
        this.documentTypes = [];
        this.reasonCodes = [];
        this.loadError = 'Admin data failed to load. Refresh and retry.';
        this.snackBar.open('Admin data failed to load.', 'Dismiss', { duration: 2600 });
        return of({ users: [], permissions: [], stateMachine: null, documentTypes: [], reasonCodes: [] });
      }))
      .subscribe({
        next: (data) => {
          this.users = data.users;
          this.permissions = data.permissions;
          this.stateMachine = data.stateMachine;
          this.documentTypes = data.documentTypes;
          this.reasonCodes = data.reasonCodes;
        }
      });
  }
}
