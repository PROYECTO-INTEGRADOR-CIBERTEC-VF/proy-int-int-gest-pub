import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

function getLoginRouteByRole(tipoRequerido: string): string {
  switch (tipoRequerido.toUpperCase()) {
    case 'FUNCIONARIO':
      return '/acceso-funcionario';
    case 'TTAIP':
      return '/acceso-ttaip';
    case 'ADMINISTRADOR':
      return '/acceso-admin';
    default:
      return '/login';
  }
}

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.estaLogueado()) {
    return true;
  }

  void router.navigate(['/login']);
  return false;
};

export const tipoUsuarioGuard = (tipoRequerido: string): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.estaLogueado()) {
      void router.navigate([getLoginRouteByRole(tipoRequerido)]);
      return false;
    }

    const tipoUsuario = authService.getTipoUsuario();
    if (tipoUsuario?.toLowerCase() === tipoRequerido.toLowerCase()) {
      return true;
    }

    void router.navigate(['/']);
    return false;
  };
};
