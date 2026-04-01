import { Component } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { map } from 'rxjs';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
  selector: 'app-loading-indicator',
  standalone: true,
  imports: [AsyncPipe, MatProgressBarModule],
  templateUrl: './loading-indicator.component.html',
  styleUrl: './loading-indicator.component.scss'
})
export class LoadingIndicatorComponent {
  readonly show$ = this.loadingService.isLoading$.pipe(map((count) => count > 0));

  constructor(private readonly loadingService: LoadingService) {}
}
