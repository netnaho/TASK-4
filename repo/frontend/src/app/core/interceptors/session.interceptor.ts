import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { CsrfService } from '../services/csrf.service';

export const sessionInterceptor: HttpInterceptorFn = (req, next) => {
  const csrfService = inject(CsrfService);
  const unsafeMethods = ['POST', 'PUT', 'PATCH', 'DELETE'];
  const headers: Record<string, string> = {};
  const token = csrfService.getToken();
  if (token && unsafeMethods.includes(req.method.toUpperCase())) {
    headers['X-XSRF-TOKEN'] = token;
  }

  return next(req.clone({ withCredentials: true, setHeaders: headers }));
};
