export enum EstadoApelacion {
  PENDIENTE_ELEVACION = 'PENDIENTE_ELEVACION',
  EN_CALIFICACION_1 = 'EN_CALIFICACION_1',
  EN_SUBSANACION = 'EN_SUBSANACION',
  EN_CALIFICACION_2 = 'EN_CALIFICACION_2',
  NOTIFICACION_SEGUNDA_CALIFICACION = 'NOTIFICACION_SEGUNDA_CALIFICACION',
  EN_RESOLUCION = 'EN_RESOLUCION',
  RESUELTO = 'RESUELTO',
  TENER_POR_NO_PRESENTADO = 'TENER_POR_NO_PRESENTADO',
  CONCLUSION_SUSTRACCION_MATERIA = 'CONCLUSION_SUSTRACCION_MATERIA',
  CONCLUSION_DESISTIMIENTO = 'CONCLUSION_DESISTIMIENTO',
  RESUELTO_FUNDADO = 'RESUELTO_FUNDADO',
  RESUELTO_FUNDADO_EN_PARTE = 'RESUELTO_FUNDADO_EN_PARTE',
  RESUELTO_INFUNDADO = 'RESUELTO_INFUNDADO',
  RESUELTO_INFUNDADO_EN_PARTE = 'RESUELTO_INFUNDADO_EN_PARTE',
  RESUELTO_IMPROCEDENTE = 'RESUELTO_IMPROCEDENTE'
}

export enum CalificacionApelacion {
  ADMISIBLE = 'ADMISIBLE',
  INADMISIBLE = 'INADMISIBLE',
  IMPROCEDENTE = 'IMPROCEDENTE',
  ADMITIDO = 'ADMITIDO'
}

export interface DocumentoApelacion {
  id?: number;
  nombreArchivo?: string;
  rutaArchivo?: string;
  tipoArchivo?: string;
}

export interface ResolucionApelacion {
  idResolucion?: number;
  numeroResolucion?: string;
  tipoResolucion?: string;
  decision?: string;
  fundamentos?: string;
  observaciones?: string;
  fechaResolucion?: string;
  miembroId?: number;
  miembroNombre?: string;
  miembroTTAIPNombre?: string;
}

export interface Apelacion {
  idApelacion: number;
  id?: number;
  expediente: string;
  solicitudId?: number;
  ciudadanoId?: number;
  fundamentos?: string;
  fechaApelacion?: string;
  estado: EstadoApelacion;
  calificacionPrimera?: CalificacionApelacion;
  calificacionSegunda?: CalificacionApelacion;
  fechaSubsanacion?: string;
  diasSubsanacion?: number;
  resultado?: string;
  ciudadanoNombre?: string;
  solicitudExpediente?: string;
  entidadNombre?: string;
  solicitudAsunto?: string;
  resolucion?: ResolucionApelacion;
  resolucionPrimeraCalificacion?: ResolucionApelacion;
  resolucionSegundaCalificacion?: ResolucionApelacion;
  documentos?: DocumentoApelacion[];
  fundamentosSubsanacion?: string;
  documentosSubsanacion?: DocumentoApelacion[];
}

export interface ApelacionCreate {
  solicitudId: number;
  ciudadanoId: number;
  fundamentos: string;
}