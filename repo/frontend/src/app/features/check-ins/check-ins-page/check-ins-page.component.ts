import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CheckInDetailModel, CheckInRevisionModel, CheckInSummaryModel } from '../../../core/models/check-in.models';
import { CheckInService } from '../../../core/services/check-in.service';

@Component({
  selector: 'app-check-ins-page',
  standalone: true,
  imports: [CommonModule, DatePipe, ReactiveFormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './check-ins-page.component.html',
  styleUrl: './check-ins-page.component.scss'
})
export class CheckInsPageComponent implements OnInit {
  checkIns: CheckInSummaryModel[] = [];
  selectedCheckIn: CheckInDetailModel | null = null;
  selectedFiles: File[] = [];
  isLocating = false;
  loadError = '';
  isCreating = false;
  isUpdating = false;

  readonly form = this.fb.group({
    commentText: [''],
    deviceTimestamp: [new Date().toISOString()],
    latitude: [null as number | null],
    longitude: [null as number | null]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly checkInService: CheckInService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  quickCreate(): void {
    if (this.isCreating) {
      return;
    }
    this.isCreating = true;
    this.checkInService.create(this.buildPayload(), this.selectedFiles).pipe(
      finalize(() => (this.isCreating = false))
    ).subscribe({
      next: (checkIn) => {
        this.selectedCheckIn = checkIn;
        this.selectedFiles = [];
        this.snackBar.open('Check-in created', 'Dismiss', { duration: 2000 });
        this.reload();
      }
    });
  }

  update(): void {
    if (!this.selectedCheckIn || this.isUpdating) {
      return;
    }
    this.isUpdating = true;
    this.checkInService.update(this.selectedCheckIn.id, this.buildPayload(), this.selectedFiles).pipe(
      finalize(() => (this.isUpdating = false))
    ).subscribe({
      next: (checkIn) => {
        this.selectedCheckIn = checkIn;
        this.selectedFiles = [];
        this.snackBar.open('Check-in updated with new revision', 'Dismiss', { duration: 2200 });
        this.reload();
      }
    });
  }

  captureGeolocation(): void {
    if (!navigator.geolocation) {
      this.snackBar.open('Geolocation is unavailable in this browser.', 'Dismiss', { duration: 2200 });
      return;
    }
    this.isLocating = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.form.patchValue({ latitude: position.coords.latitude, longitude: position.coords.longitude });
        this.isLocating = false;
      },
      () => {
        this.isLocating = false;
        this.snackBar.open('Geolocation permission was denied or unavailable.', 'Dismiss', { duration: 2200 });
      }
    );
  }

  selectCheckIn(id: number): void {
    this.checkInService.get(id).subscribe({
      next: (checkIn) => {
        this.selectedCheckIn = checkIn;
        this.form.patchValue({
          commentText: checkIn.commentText ?? '',
          deviceTimestamp: checkIn.deviceTimestamp,
          latitude: checkIn.latitude ?? null,
          longitude: checkIn.longitude ?? null
        });
      }
    });
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = Array.from(input.files ?? []);
  }

  highlightClass(revision: CheckInRevisionModel, field: string): string {
    return revision.changedFields.includes(field) ? 'changed' : '';
  }

  attachmentUrl(attachmentId: number): string {
    if (!this.selectedCheckIn) {
      return '';
    }
    return this.checkInService.attachmentUrl(this.selectedCheckIn.id, attachmentId);
  }

  private buildPayload(): string {
    return JSON.stringify({
      commentText: this.form.controls.commentText.value,
      deviceTimestamp: this.form.controls.deviceTimestamp.value,
      latitude: this.form.controls.latitude.value,
      longitude: this.form.controls.longitude.value
    });
  }

  private reload(): void {
    this.loadError = '';
    this.checkInService.list().pipe(
      catchError(() => {
        this.loadError = 'Field check-ins could not be loaded right now.';
        return of({ content: [] as CheckInSummaryModel[], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true });
      })
    ).subscribe({ next: (page) => (this.checkIns = page.content) });
  }
}
