import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { AuthService } from '../services/auth.service';
import { authGuard, tipoUsuarioGuard } from './auth.guard';

describe('authGuard', () => {
  const authServiceMock = {
    estaLogueado: vi.fn(),
    getTipoUsuario: vi.fn(),
  };
  const routerMock = {
    navigate: vi.fn().mockResolvedValue(true),
  };

  beforeEach(() => {
    authServiceMock.estaLogueado.mockReset();
    authServiceMock.getTipoUsuario.mockReset();
    routerMock.navigate.mockReset();
    routerMock.navigate.mockResolvedValue(true);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  function runAuthGuard() {
    return TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
  }

  function runTipoUsuarioGuard(tipoRequerido: string) {
    return TestBed.runInInjectionContext(() =>
      tipoUsuarioGuard(tipoRequerido)({} as never, {} as never),
    );
  }

  it('permite acceso cuando el usuario esta logueado', () => {
    authServiceMock.estaLogueado.mockReturnValue(true);

    const result = runAuthGuard();

    expect(result).toBe(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('deniega acceso cuando no esta logueado y redirige a /login', () => {
    authServiceMock.estaLogueado.mockReturnValue(false);

    const result = runAuthGuard();

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('tipoUsuarioGuard permite acceso con comparacion case-insensitive', () => {
    authServiceMock.estaLogueado.mockReturnValue(true);
    authServiceMock.getTipoUsuario.mockReturnValue('funcionario');

    const result = runTipoUsuarioGuard('FUNCIONARIO');

    expect(result).toBe(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('tipoUsuarioGuard deniega acceso por rol distinto y redirige a /', () => {
    authServiceMock.estaLogueado.mockReturnValue(true);
    authServiceMock.getTipoUsuario.mockReturnValue('CIUDADANO');

    const result = runTipoUsuarioGuard('TTAIP');

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });

  it('tipoUsuarioGuard redirige al login correcto por rol cuando no esta logueado', () => {
    authServiceMock.estaLogueado.mockReturnValue(false);

    const result = runTipoUsuarioGuard('TTAIP');

    expect(result).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/acceso-ttaip']);
  });
});
