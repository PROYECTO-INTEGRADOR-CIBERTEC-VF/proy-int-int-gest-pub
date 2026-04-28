export type TipoUsuario = 'CIUDADANO' | 'FUNCIONARIO' | 'TTAIP' | 'ADMINISTRADOR';

export interface Usuario {
  id: number;
  email: string;
  tipoUsuario: TipoUsuario;
  activo: boolean;
}

export interface LoginRequestDTO {
  identificador: string;
  password: string;
}

export type LoginRequest = LoginRequestDTO;

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

export interface RegistroRequestDTO {
  email: string;
  password: string;
  dni: string;
  nombreCompleto: string;
  telefono?: string;
  direccion?: string;
}

export type RegistroRequest = RegistroRequestDTO;

export interface RegistroResponse {
  success: boolean;
  mensaje?: string;
  ciudadanoId?: number;
  email?: string;
}
