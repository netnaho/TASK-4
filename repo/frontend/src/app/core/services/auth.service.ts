import { Injectable } from '@angular/core';
import { HttpContext } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, of, tap } from 'rxjs';
import { ApiService } from './api.service';
import { CsrfService } from './csrf.service';
import {
  ApiErrorModel,
  CaptchaChallengeModel,
  LoginRequestModel,
  LoginResponseModel,
  UserSessionModel
} from '../models/auth.models';
import { SKIP_ERROR_TOAST } from '../interceptors/http-context.tokens';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSubject = new BehaviorSubject<UserSessionModel | null | undefined>(undefined);
  private readonly bootstrappingSubject = new BehaviorSubject<boolean>(false);

  readonly user$ = this.userSubject.asObservable();
  readonly bootstrapping$ = this.bootstrappingSubject.asObservable();

  constructor(
    private readonly apiService: ApiService,
    private readonly csrfService: CsrfService
  ) {}

  initialize(): Promise<void> {
    this.bootstrappingSubject.next(true);
    return new Promise<void>((resolve) => {
      this.fetchCsrfToken().subscribe({
        next: () => {
          this.me().subscribe({
            next: () => resolve(undefined),
            error: () => resolve(undefined)
          });
        },
        error: () => resolve(undefined)
      });
    }).finally(() => this.bootstrappingSubject.next(false));
  }

  fetchCsrfToken(): Observable<unknown> {
    return this.apiService.get<{ token: string }>('/auth/csrf', { context: new HttpContext().set(SKIP_ERROR_TOAST, true) }).pipe(
      tap((response) => this.csrfService.setToken(response.token))
    );
  }

  login(payload: LoginRequestModel): Observable<LoginResponseModel> {
    return this.apiService.post<LoginResponseModel>('/auth/login', payload).pipe(
      tap((response) => this.userSubject.next(response.user))
    );
  }

  logout(): Observable<unknown> {
    return this.apiService.post('/auth/logout', {}).pipe(
      tap(() => this.userSubject.next(null))
    );
  }

  me(): Observable<UserSessionModel | null> {
    return this.apiService.get<UserSessionModel>('/auth/me', { context: new HttpContext().set(SKIP_ERROR_TOAST, true) }).pipe(
      tap((user) => this.userSubject.next(user)),
      catchError(() => {
        this.userSubject.next(null);
        return of(null);
      })
    );
  }

  getCaptcha(username: string): Observable<CaptchaChallengeModel> {
    return this.apiService.get<CaptchaChallengeModel>(`/auth/captcha?username=${encodeURIComponent(username)}`, {
      context: new HttpContext().set(SKIP_ERROR_TOAST, true)
    });
  }

  userSnapshot(): UserSessionModel | null | undefined {
    return this.userSubject.value;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.userSubject.value;
    return !!user && roles.includes(user.role);
  }

  ensureInitialized(): Observable<UserSessionModel | null> {
    const current = this.userSubject.value;
    if (current !== undefined) {
      return of(current ?? null);
    }
    return this.me();
  }

  extractApiError(error: unknown): ApiErrorModel | null {
    if (!error || typeof error !== 'object' || !('error' in error)) {
      return null;
    }
    return (error as { error: ApiErrorModel }).error;
  }
}
