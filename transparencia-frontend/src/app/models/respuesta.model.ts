export type TipoRespuesta =
  | 'ENTREGA_TOTAL'
  | 'ENTREGA_PARCIAL'
  | 'DENEGACION_TOTAL'
  | 'SILENCIO_ADMINISTRATIVO';

export interface Respuesta {
  id: number;
  tipoRespuesta?: TipoRespuesta | null;
  decision?: string | null;
  contenido?: string | null;
  causalDenegatoria?: string | null;
  fundamentoLegal?: string | null;
  fechaRespuesta?: string | null;
  funcionarioId?: number | null;
  funcionarioNombre?: string | null;
}

export interface CrearRespuestaRequest {
  solicitudId: number;
  funcionarioId: number;
  tipoRespuesta: TipoRespuesta;
  contenido: string;
  causalDenegatoria?: string | null;
  fundamentoLegal?: string | null;
  formatoEntrega?: string | null;
  plazoEntrega?: number | null;
}
