import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { TokenService } from '../services/token.service';
import { environment } from '../../environments/environment';

export const jwtInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  const storageService = inject(TokenService);
  const myToken = storageService.getToken();
const isApiRequest = req.url.startsWith(environment.apiUrl);

  if (myToken && isApiRequest) {
    const cloneRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${myToken}`
      }
    });
    return next(cloneRequest);
  }
  return next(req);
};
