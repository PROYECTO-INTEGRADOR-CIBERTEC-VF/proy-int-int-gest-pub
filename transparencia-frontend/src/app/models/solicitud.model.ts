import { Respuesta } from './respuesta.model';

export type EstadoSolicitud =
  | 'RECEPCIONADA'
  | 'EN_REVISION'
  | 'PENDIENTE_INFORMACION'
  | 'RESPONDIDA'
  | 'DENEGADA'
  | 'CONCLUIDA'
  | 'VENCIDA';

export type Prioridad = 'BAJA' | 'NORMAL' | 'MEDIA' | 'ALTA' | 'URGENTE';

export type SemaforoEstado = 'VERDE' | 'AMBAR' | 'ROJO' | 'NEGRO';

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
