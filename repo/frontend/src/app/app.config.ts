import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { apiBaseInterceptor } from './core/interceptors/api-base.interceptor';
import { loadingInterceptor } from './core/interceptors/loading.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { sessionInterceptor } from './core/interceptors/session.interceptor';
import { AuthService } from './core/services/auth.service';
import { IconService } from './core/services/icon.service';

function initializeAuth(authService: AuthService) {
  return () => authService.initialize();
}

function initializeIcons(iconService: IconService) {
  return () => iconService.registerIcons();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(withInterceptors([apiBaseInterceptor, sessionInterceptor, loadingInterceptor, errorInterceptor])),
    { provide: APP_INITIALIZER, useFactory: initializeAuth, deps: [AuthService], multi: true },
    { provide: APP_INITIALIZER, useFactory: initializeIcons, deps: [IconService], multi: true }
  ]
};
