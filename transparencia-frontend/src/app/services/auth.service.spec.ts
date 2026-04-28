import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import {
  LoginRequest,
  LoginResponse,
  RegistroRequest,
  RegistroResponse,
} from '../models/usuario.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.removeItem('usuario');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('usuario');
  });

  it('deberia crear el servicio', () => {
    expect(service).toBeTruthy();
  });

  it('deberia hacer login con POST /api/auth/login', () => {
    const payload: LoginRequest = {
      identificador: '12345678',
      password: 'Clave123',
    };
    const response: LoginResponse = {
      success: true,
      token: 'jwt-test-token',
      tipoUsuario: 'CIUDADANO',
      usuarioId: 1,
      email: 'ciudadano@saip.gob.pe',
    };

    service.login(payload).subscribe((res) => {
      expect(res).toEqual(response);
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(response);
  });

  it('deberia registrar usuario con POST /api/auth/registro', () => {
    const payload: RegistroRequest = {
      email: 'nuevo@saip.gob.pe',
      password: 'Clave123',
      dni: '12345678',
      nombreCompleto: 'Ciudadano Prueba',
      telefono: '999999999',
      direccion: 'Lima',
    };
    const response: RegistroResponse = {
      success: true,
      mensaje: 'Registro exitoso',
      ciudadanoId: 10,
      email: 'nuevo@saip.gob.pe',
    };

    service.registro(payload).subscribe((res) => {
      expect(res).toEqual(response);
    });

    const req = httpMock.expectOne('/api/auth/registro');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(response);
  });

  it('deberia guardar y obtener sesion en localStorage', () => {
    const sesion: LoginResponse = {
      success: true,
      token: 'jwt-session-token',
      tipoUsuario: 'CIUDADANO',
      usuarioId: 5,
      email: 'usuario@saip.gob.pe',
    };

    service.guardarSesion(sesion);
    const dataGuardada = localStorage.getItem('usuario');

    expect(dataGuardada).not.toBeNull();
    expect(service.obtenerSesion()).toEqual(sesion);
  });

  it('deberia exponer token y tipo de usuario desde la sesion', () => {
    const sesion: LoginResponse = {
      success: true,
      token: 'jwt-token-123',
      tipoUsuario: 'FUNCIONARIO',
      usuarioId: 20,
    };

    service.guardarSesion(sesion);

    expect(service.estaLogueado()).toBe(true);
    expect(service.getToken()).toBe('jwt-token-123');
    expect(service.getTipoUsuario()).toBe('FUNCIONARIO');
  });

  it('deberia limpiar sesion y retornar token/tipoUsuario nulos', () => {
    service.guardarSesion({
      success: true,
      token: 'jwt-any',
      tipoUsuario: 'ADMINISTRADOR',
    });

    service.cerrarSesion();

    expect(localStorage.getItem('usuario')).toBeNull();
    expect(service.estaLogueado()).toBe(false);
    expect(service.getToken()).toBeNull();
    expect(service.getTipoUsuario()).toBeNull();
  });
});
