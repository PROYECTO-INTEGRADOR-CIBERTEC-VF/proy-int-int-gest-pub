import type { components } from '../api/schema';
import { Respuesta } from './respuesta.model';

type SolicitudDTO = components['schemas']['SolicitudDTO'];

export type EstadoSolicitud = Exclude<SolicitudDTO['estado'], undefined>;

export type Prioridad = Exclude<SolicitudDTO['prioridad'], undefined>;

export type SemaforoEstado = Exclude<SolicitudDTO['semaforo'], undefined>;

export interface Solicitud {
  idSolicitud: number;
  expediente: string;
  asunto: string;
  descripcion: string;
  fechaPresentacion?: string;
  fechaLimite?: string;
  estado: EstadoSolicitud;
  prioridad?: Prioridad;
  tipoInformacion?: string | null;
  formatoDigital?: boolean;
  formatoFisico?: boolean;
  copiaSimple?: boolean;
  copiaCertificada?: boolean;
  ciudadanoId?: number;
  entidadId?: number;
  ciudadanoNombre?: string;
  entidadNombre?: string;
  respuesta?: Respuesta | null;
  diasRestantes?: number;
  semaforo?: SemaforoEstado;
}

export interface SolicitudCreate {
  asunto: string;
  descripcion: string;
  ciudadanoId: number;
  entidadId: number;
  tipoInformacion?: string;
  formatoDigital?: boolean;
  formatoFisico?: boolean;
  copiaSimple?: boolean;
  copiaCertificada?: boolean;
}
