import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { SKIP_ERROR_TOAST } from './http-context.tokens';

type BackendError = {
  message?: string;
  details?: string[];
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  return next(req).pipe(
    catchError((error) => {
      if (req.context.get(SKIP_ERROR_TOAST)) {
        return throwError(() => error);
      }
      const payload = error?.error as BackendError | undefined;
      const message = payload?.message ?? 'Service is temporarily unavailable';
      snackBar.open(message, 'Dismiss', { duration: 4000, panelClass: ['error-toast'] });
      return throwError(() => error);
    })
  );
};
