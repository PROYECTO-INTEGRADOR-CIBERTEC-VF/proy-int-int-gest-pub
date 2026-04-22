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

export interface Apelacion {
  idApelacion?: number;
  id?: number;
  expediente?: string;
  solicitudId?: number;
  solicitudExpediente?: string;
  ciudadanoId?: number;
  entidadNombre?: string;
  fundamentos: string;
  fechaApelacion: string;
  estado: EstadoApelacion;
  calificacionPrimera?: CalificacionApelacion;
  calificacionSegunda?: CalificacionApelacion;
  fechaSubsanacion?: string;
  diasSubsanacion?: number;
  resultado?: string;
}

export interface ApelacionCreate {
  solicitudId: number;
  ciudadanoId: number;
  fundamentos: string;
}