import { HttpClient } from '@angular/common/http';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse, RegistroRequest, RegistroResponse } from '../models/usuario.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly apiUrl = '/api/auth';

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials);
  }

  registro(data: RegistroRequest): Observable<RegistroResponse> {
    return this.http.post<RegistroResponse>(`${this.apiUrl}/registro`, data);
  }

  guardarSesion(data: LoginResponse): void {
    if (this.isBrowser && typeof localStorage !== 'undefined') {
      localStorage.setItem('usuario', JSON.stringify(data));
    }
  }

  obtenerSesion(): LoginResponse | null {
    if (!this.isBrowser || typeof localStorage === 'undefined') {
      return null;
    }

    const data = localStorage.getItem('usuario');
    return data ? JSON.parse(data) as LoginResponse : null;
  }

  cerrarSesion(): void {
    if (this.isBrowser && typeof localStorage !== 'undefined') {
      localStorage.removeItem('usuario');
    }
  }

  estaLogueado(): boolean {
    const sesion = this.obtenerSesion();
    return Boolean(sesion?.token);
  }

  getTipoUsuario(): string | null {
    const sesion = this.obtenerSesion();
    return sesion?.tipoUsuario ? String(sesion.tipoUsuario) : null;
  }
}
