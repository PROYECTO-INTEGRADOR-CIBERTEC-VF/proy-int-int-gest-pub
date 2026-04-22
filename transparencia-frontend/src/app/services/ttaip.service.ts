import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { forkJoin, map } from 'rxjs';
import { Apelacion } from '../models/apelacion.model';

interface CalificacionRequest {
  fundamentos: string;
  miembroId?: number;
  observaciones?: string;
  diasSubsanacion?: number;
}

interface ResolucionRequest {
  fundamentos: string;
  iniciarProcesoDisciplinario?: boolean;
}

interface TtaipEstadisticasResponse {
  total?: number;
  pendientes?: number;
  pendientesAdmision?: number;
  enProceso?: number;
  enSubsanacion?: number;
  resueltas?: number;
}

@Injectable({
  providedIn: 'root'
})
export class TtaipService {
  private readonly apiUrl = 'http://localhost:8080/api/ttaip';
  private readonly resolucionUrl = `${this.apiUrl}/resolucion`;

  constructor(private readonly http: HttpClient) { }

  getEstadisticas(): Observable<TtaipEstadisticasResponse> {
    return this.http.get<TtaipEstadisticasResponse>(`${this.apiUrl}/estadisticas`);
  }

  getPendientes(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/pendientes`);
  }

  getEnAnalisis(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/en-calificacion`);
  }

  getSubsanacion(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/subsanacion`);
  }

  getSegundaCalificacion(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/segunda-calificacion`);
  }

  getEnResolucion(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/en-resolucion`);
  }

  getResueltas(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/resueltas`);
  }

  getApelacionPorExpediente(expediente: string): Observable<Apelacion | null> {
    return forkJoin([
      this.getPendientes(),
      this.getEnAnalisis(),
      this.getSubsanacion(),
      this.getSegundaCalificacion(),
      this.getEnResolucion(),
      this.getResueltas(),
    ]).pipe(
      map((listas) => listas.flat().find((apelacion) => apelacion.expediente === expediente) ?? null)
    );
  }

  admitirApelacion(id: number, data: CalificacionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.apiUrl}/calificacion/${id}/admitir`, data);
  }

  requerirSubsanacion(id: number, data: CalificacionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.apiUrl}/calificacion/${id}/subsanar`, data);
  }

  inadmitirApelacion(id: number, data: CalificacionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.apiUrl}/calificacion/${id}/inadmitir`, data);
  }

  declararFundado(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/fundado`, data);
  }

  declararInfundado(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/infundado`, data);
  }

  declararFundadoEnParte(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/fundado-en-parte`, data);
  }

  declararInfundadoEnParte(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/infundado-en-parte`, data);
  }

  declararImprocedente(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/improcedente`, data);
  }

  declararSustraccionMateria(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/sustraccion-materia`, data);
  }

  declararDesistimiento(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/desistimiento`, data);
  }

  declararNoPresentado(apelacionId: number, data: ResolucionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.resolucionUrl}/${apelacionId}/no-presentado`, data);
  }
}
