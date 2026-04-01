import { Component } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-discrepancy-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: '<h2 mat-dialog-title>Receipt discrepancy</h2><mat-dialog-content>Received quantities differ from shipment expectations. Confirm to record a discrepancy.</mat-dialog-content><mat-dialog-actions align="end"><button mat-button (click)="close(false)">Cancel</button><button mat-flat-button color="warn" (click)="close(true)">Confirm discrepancy</button></mat-dialog-actions>'
})
export class DiscrepancyDialogComponent {
  constructor(private readonly dialogRef: MatDialogRef<DiscrepancyDialogComponent>) {}

  close(value: boolean): void {
    this.dialogRef.close(value);
  }
}
