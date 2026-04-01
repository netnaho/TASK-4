import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, finalize, forkJoin, map, of } from 'rxjs';
import { DocumentCenterService } from '../../../core/services/document-center.service';
import {
  DocumentArchiveModel,
  DocumentDetailModel,
  DocumentPreviewModel,
  DocumentSummaryModel,
  DocumentTemplateModel,
  DocumentTypeModel
} from '../../../core/models/document.models';
import { AuthService } from '../../../core/services/auth.service';
import { CriticalActionService } from '../../../core/services/critical-action.service';

@Component({
  selector: 'app-document-center',
  standalone: true,
  imports: [CommonModule, DatePipe, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatDialogModule, MatPaginatorModule],
  templateUrl: './document-center.component.html',
  styleUrl: './document-center.component.scss'
})
export class DocumentCenterComponent implements OnInit {
  private readonly dialog = inject(MatDialog);

  readonly authUser = this.authService.userSnapshot();
  readonly canManageTemplates = this.authUser?.role === 'SYSTEM_ADMINISTRATOR';
  readonly canApprove = this.authUser?.role === 'QUALITY_REVIEWER' || this.authUser?.role === 'SYSTEM_ADMINISTRATOR';
  readonly canArchive = this.authUser?.role === 'SYSTEM_ADMINISTRATOR';
  readonly canRequestCriticalAction = this.authUser?.role === 'BUYER' || this.authUser?.role === 'SYSTEM_ADMINISTRATOR';

  documentTypes: DocumentTypeModel[] = [];
  templates: DocumentTemplateModel[] = [];
  documents: DocumentSummaryModel[] = [];
  approvalQueue: DocumentSummaryModel[] = [];
  archives: DocumentArchiveModel[] = [];
  selectedDocument: DocumentDetailModel | null = null;
  selectedFile: File | null = null;
  isLoading = true;
  loadError = '';
  private readonly pendingActions = new Set<string>();

  readonly draftForm = this.fb.group({
    documentTypeId: [null as number | null, Validators.required],
    templateId: [null as number | null],
    title: ['', Validators.required],
    metadataTags: ['regulated, internal'],
    contentText: [''],
    approvalRoles: [['QUALITY_REVIEWER'] as string[], Validators.required]
  });

  readonly templateForm = this.fb.group({
    documentTypeId: [null as number | null, Validators.required],
    templateName: ['', Validators.required],
    templateBody: [''],
    active: [true]
  });

  constructor(
    private readonly fb: FormBuilder,
    public readonly documentCenterService: DocumentCenterService,
    private readonly snackBar: MatSnackBar,
    private readonly authService: AuthService,
    private readonly criticalActionService: CriticalActionService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.item(0) ?? null;
  }

  createTemplate(): void {
    if (!this.startAction(this.templateActionKey())) {
      return;
    }
    if (this.templateForm.invalid) {
      this.finishAction(this.templateActionKey());
      this.templateForm.markAllAsTouched();
      return;
    }
    this.documentCenterService.createTemplate(this.templateForm.getRawValue()).pipe(
      finalize(() => this.finishAction(this.templateActionKey()))
    ).subscribe({
      next: () => {
        this.snackBar.open('Template created', 'Dismiss', { duration: 2200 });
        this.reload();
      }
    });
  }

  createDraft(): void {
    if (!this.startAction(this.draftActionKey())) {
      return;
    }
    if (this.draftForm.invalid) {
      this.finishAction(this.draftActionKey());
      this.draftForm.markAllAsTouched();
      return;
    }
    this.documentCenterService.createDraft(JSON.stringify(this.draftForm.getRawValue()), this.selectedFile).pipe(
      finalize(() => this.finishAction(this.draftActionKey()))
    ).subscribe({
      next: (document) => {
        this.selectedDocument = document;
        this.snackBar.open('Draft created', 'Dismiss', { duration: 2200 });
        this.reload();
      }
    });
  }

  openDocument(id: number): void {
    this.documentCenterService.getDocument(id).subscribe({ next: (document) => (this.selectedDocument = document) });
  }

  submitForApproval(id: number): void {
    const actionKey = this.documentActionKey('submit', id);
    if (!this.startAction(actionKey)) {
      return;
    }
    this.documentCenterService.submitForApproval(id).pipe(
      finalize(() => this.finishAction(actionKey))
    ).subscribe({
      next: (document) => {
        this.selectedDocument = document;
        this.snackBar.open('Document submitted for approval', 'Dismiss', { duration: 2200 });
        this.reload();
      }
    });
  }

  approve(id: number): void {
    const actionKey = this.documentActionKey('approve', id);
    if (!this.startAction(actionKey)) {
      return;
    }
    this.documentCenterService.approve(id, 'Approved from Quality Reviewer queue.').pipe(
      finalize(() => this.finishAction(actionKey))
    ).subscribe({
      next: (document) => {
        this.selectedDocument = document;
        this.snackBar.open('Document approved', 'Dismiss', { duration: 2200 });
        this.reload();
      }
    });
  }

  archive(id: number): void {
    const actionKey = this.documentActionKey('archive', id);
    if (!this.startAction(actionKey)) {
      return;
    }
    this.documentCenterService.archive(id).pipe(
      finalize(() => this.finishAction(actionKey))
    ).subscribe({
      next: (document) => {
        this.selectedDocument = document;
        this.snackBar.open('Document archived', 'Dismiss', { duration: 2200 });
        this.reload();
      }
    });
  }

  preview(id: number): void {
    const actionKey = this.documentActionKey('preview', id);
    if (!this.startAction(actionKey)) {
      return;
    }
    forkJoin({ preview: this.documentCenterService.preview(id), blob: this.documentCenterService.fetchContentBlob(id) }).pipe(
      finalize(() => this.finishAction(actionKey))
    ).subscribe({
      next: ({ preview, blob }) => {
        const objectUrl = URL.createObjectURL(blob);
        const dialogRef = this.dialog.open(DocumentPreviewDialogComponent, {
          width: '960px',
          data: { preview, previewObjectUrl: objectUrl }
        });
        dialogRef.afterClosed().subscribe(() => URL.revokeObjectURL(objectUrl));
      },
      error: () => this.snackBar.open('Preview is unavailable for this document.', 'Dismiss', { duration: 2600 })
    });
  }

  requestDestruction(id: number): void {
    const actionKey = this.documentActionKey('request-destruction', id);
    if (!this.startAction(actionKey)) {
      return;
    }
    this.criticalActionService.create({ requestType: 'DOCUMENT_DESTRUCTION', targetType: 'DOCUMENT', targetId: id, justification: 'Document destruction requires dual approval.' })
      .pipe(finalize(() => this.finishAction(actionKey)))
      .subscribe({ next: () => this.snackBar.open('Document destruction request created', 'Dismiss', { duration: 2200 }) });
  }

  requestRetentionOverride(id: number): void {
    const actionKey = this.documentActionKey('request-retention', id);
    if (!this.startAction(actionKey)) {
      return;
    }
    this.criticalActionService.create({ requestType: 'RETENTION_OVERRIDE', targetType: 'DOCUMENT', targetId: id, justification: 'Retention override requested for controlled record.' })
      .pipe(finalize(() => this.finishAction(actionKey)))
      .subscribe({ next: () => this.snackBar.open('Retention override request created', 'Dismiss', { duration: 2200 }) });
  }

  isActionPending(actionKey: string): boolean {
    return this.pendingActions.has(actionKey);
  }

  draftActionKey(): string {
    return 'draft:create';
  }

  templateActionKey(): string {
    return 'template:create';
  }

  documentActionKey(action: string, id: number): string {
    return `${action}:${id}`;
  }

  private reload(): void {
    this.isLoading = true;
    this.loadError = '';
    const emptyPage = { content: [] as DocumentSummaryModel[], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true };
    forkJoin({
      types: this.documentCenterService.listTypes().pipe(catchError(() => of([]))),
      templates: this.documentCenterService.listTemplates().pipe(catchError(() => of([]))),
      documents: this.documentCenterService.listDocuments().pipe(catchError(() => of(emptyPage))),
      queue: (this.canApprove ? this.documentCenterService.approvalQueue() : of(emptyPage)).pipe(catchError(() => of(emptyPage))),
      archives: this.documentCenterService.archiveList().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ types, templates, documents, queue, archives }) => {
        this.documentTypes = types;
        this.templates = templates;
        this.documents = documents.content;
        this.approvalQueue = queue.content;
        this.archives = archives;
        this.isLoading = false;
      }
    });
  }

  private startAction(actionKey: string): boolean {
    if (this.pendingActions.has(actionKey)) {
      return false;
    }
    this.pendingActions.add(actionKey);
    return true;
  }

  private finishAction(actionKey: string): void {
    this.pendingActions.delete(actionKey);
  }

}

@Component({
  selector: 'app-document-preview-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  templateUrl: './document-preview-dialog.component.html',
  styleUrl: './document-preview-dialog.component.scss'
})
export class DocumentPreviewDialogComponent {
  readonly data = inject<{ preview: DocumentPreviewModel; previewObjectUrl: string }>(MAT_DIALOG_DATA);
}
