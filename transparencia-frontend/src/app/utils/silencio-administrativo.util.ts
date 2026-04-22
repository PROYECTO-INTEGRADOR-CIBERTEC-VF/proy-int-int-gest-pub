import { Solicitud } from '../models/solicitud.model';

export function isSilencioAdministrativo(solicitud: Solicitud): boolean {
  // Simple heuristic: if the state is RESPONDIDA and more than 30 days passed, treat as silencio administrativo
  if (!solicitud?.fechaPresentacion) return false;
  try {
    const fecha = new Date(solicitud.fechaPresentacion);
    const hoy = new Date();
    const diffDays = Math.floor((hoy.getTime() - fecha.getTime()) / (1000 * 60 * 60 * 24));
    return (solicitud.estado === 'RESPONDIDA' || solicitud.estado === 'VENCIDA') && diffDays > 30;
  } catch {
    return false;
  }
}
