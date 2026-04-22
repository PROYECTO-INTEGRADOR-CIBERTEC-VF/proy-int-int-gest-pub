import { Apelacion } from './apelacion.model';

export interface Solicitud {
  id: number;
  expediente: string;
  entidadNombre: string;
  asunto: string;
  fechaPresentacion: string; // ISO date string
  estado: string;
  // in some responses this may be an object; allow flexible typing for now
  apelacion?: any;
}
