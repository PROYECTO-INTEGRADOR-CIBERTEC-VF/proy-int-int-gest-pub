export interface Entidad {
  idEntidad: number;
  nombre: string;
  acronimo: string;
  descripcion?: string | null;
  direccion?: string | null;
  telefono?: string | null;
  email?: string | null;
  estado?: string | null;
  activa?: boolean;
}
