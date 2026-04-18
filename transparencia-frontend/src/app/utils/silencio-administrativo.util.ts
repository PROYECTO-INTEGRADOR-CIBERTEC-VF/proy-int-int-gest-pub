export interface SolicitudSilencioContext {
  estado?: string | null;
  diasRestantes?: number | null;
  respuesta?: {
    tipoRespuesta?: string | null;
  } | null;
}

export function isSilencioAdministrativo(
  context: SolicitudSilencioContext | null | undefined,
): boolean {
  if (!context) {
    return false;
  }

  const tipoRespuesta = context.respuesta?.tipoRespuesta?.toUpperCase();
  if (tipoRespuesta === 'SILENCIO_ADMINISTRATIVO') {
    return true;
  }

  if ((context.estado ?? '').toUpperCase() === 'VENCIDA') {
    return true;
  }

  return (context.diasRestantes ?? 0) < 0;
}

export function canSendManualResponse(
  context: SolicitudSilencioContext | null | undefined,
): boolean {
  if (!context) {
    return false;
  }

  if (isSilencioAdministrativo(context)) {
    return false;
  }

  return !context.respuesta?.tipoRespuesta;
}
