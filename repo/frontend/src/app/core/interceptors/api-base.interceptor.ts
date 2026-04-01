import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export const apiBaseInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith('http')) {
    return next(req);
  }

  const normalizedPath = req.url.startsWith('/') ? req.url : `/${req.url}`;
  const cloned = req.clone({ url: `${environment.apiBaseUrl}${normalizedPath}` });
  return next(cloned);
};
