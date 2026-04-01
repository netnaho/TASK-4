import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pendingCount = new BehaviorSubject<number>(0);
  readonly isLoading$ = this.pendingCount.asObservable();

  begin(): void {
    this.pendingCount.next(this.pendingCount.value + 1);
  }

  end(): void {
    this.pendingCount.next(Math.max(this.pendingCount.value - 1, 0));
  }
}
