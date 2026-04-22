import { isPlatformBrowser } from '@angular/common';
import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  const usuarioData = localStorage.getItem('usuario');
  if (!usuarioData) {
    return next(req);
  }

  try {
    const usuario = JSON.parse(usuarioData);
    if (
      typeof usuario !== 'object' ||
      usuario === null ||
      !('token' in usuario) ||
      typeof usuario.token !== 'string' ||
      !usuario.token.trim()
    ) {
      return next(req);
    }

    return next(
      req.clone({
        setHeaders: {
          Authorization: `Bearer ${usuario.token}`
        }
      })
    );
  } catch {
    return next(req);
  }
};
