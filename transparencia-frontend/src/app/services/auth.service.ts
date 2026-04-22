import { Injectable } from '@angular/core';

export interface Sesion {
  ciudadanoId?: number;
  nombre?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Lightweight session accessor used by UI. In production this should validate tokens.
  obtenerSesion(): Sesion | null {
    try {
      const raw = localStorage.getItem('sesion');
      if (!raw) return { ciudadanoId: 1 } as Sesion;
      return JSON.parse(raw) as Sesion;
    } catch {
      return { ciudadanoId: 1 } as Sesion;
    }
  }

  cerrarSesion(): void {
    try {
      localStorage.removeItem('sesion');
    } catch {
      // ignore
    }
  }
}
