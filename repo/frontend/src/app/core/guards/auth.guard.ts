import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return authService.ensureInitialized().pipe(
    map((user) => {
      if (!user) {
        router.navigate(['/login']);
        return false;
      }
      return true;
    })
  );
};
