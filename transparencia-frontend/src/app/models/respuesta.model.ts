import type { components } from '../api/schema';

type RespuestaDTO = components['schemas']['RespuestaDTO'];
type CrearRespuestaRequestDTO = components['schemas']['CrearRespuestaRequest'];

export type TipoRespuesta = Exclude<RespuestaDTO['tipoRespuesta'], undefined>;

export interface Respuesta extends RespuestaDTO {
  id: number;
}

export interface CrearRespuestaRequest extends Omit<
  CrearRespuestaRequestDTO,
  'solicitudId' | 'funcionarioId' | 'tipoRespuesta' | 'contenido'
> {
  solicitudId: number;
  funcionarioId: number;
  tipoRespuesta: TipoRespuesta;
  contenido: string;
}
