import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const roles = (route.data['roles'] as string[] | undefined) ?? [];

  return authService.ensureInitialized().pipe(
    map((user) => {
      if (!user) {
        router.navigate(['/login']);
        return false;
      }
      if (roles.length > 0 && !roles.includes(user.role)) {
        router.navigate(['/unauthorized']);
        return false;
      }
      return true;
    })
  );
};
