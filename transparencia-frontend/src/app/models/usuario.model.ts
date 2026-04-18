export type TipoUsuario = 'CIUDADANO' | 'FUNCIONARIO' | 'TTAIP' | 'ADMINISTRADOR';

export interface LoginRequest {
  identificador: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  mensaje?: string;
  token?: string;
  usuarioId?: number;
  email?: string;
  tipoUsuario?: TipoUsuario | string;
  nombre?: string;
  dni?: string;
  ciudadanoId?: number;
  funcionarioId?: number;
  entidadId?: number;
  entidadNombre?: string;
  miembroId?: number;
  administradorId?: number;
  redirectUrl?: string;
}

export interface RegistroRequest {
  email: string;
  password: string;
  dni: string;
  nombreCompleto: string;
  telefono?: string;
  direccion?: string;
}

export interface RegistroResponse {
  success: boolean;
  mensaje?: string;
  ciudadanoId?: number;
  email?: string;
}
