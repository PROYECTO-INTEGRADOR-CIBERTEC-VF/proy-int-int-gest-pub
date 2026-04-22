import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apelacion } from '../models/apelacion.model';

interface CalificacionRequest {
  fundamentos: string;
  miembroId?: number;
  observaciones?: string;
  diasSubsanacion?: number;
}

@Injectable({
  providedIn: 'root'
})
export class TtaipService {
  // Combinamos ambas URLs para que funcionen las peticiones de ambos
  private readonly apiUrl = 'http://localhost:8080/api/ttaip';
  private baseUrl = '/api/ttaip/resolucion';

  constructor(private readonly http: HttpClient) { }

  // ==========================================
  // MÉTODOS DE TUS COMPAÑEROS (develop)
  // ==========================================
  getEstadisticas(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/estadisticas`);
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

  admitirApelacion(id: number, data: CalificacionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.apiUrl}/calificacion/${id}/admitir`, data);
  }

  requerirSubsanacion(id: number, data: CalificacionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.apiUrl}/calificacion/${id}/subsanar`, data);
  }

  inadmitirApelacion(id: number, data: CalificacionRequest): Observable<Apelacion> {
    return this.http.post<Apelacion>(`${this.apiUrl}/calificacion/${id}/inadmitir`, data);
  }

  // ==========================================
  // TUS MÉTODOS DE RESOLUCIÓN FINAL (HU-08)
  // ==========================================
  declararFundado(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/fundado`, data);
  }

  declararInfundado(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/infundado`, data);
  }

  declararFundadoEnParte(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/fundado-en-parte`, data);
  }

  declararInfundadoEnParte(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/infundado-en-parte`, data);
  }

  declararImprocedente(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/improcedente`, data);
  }

  declararSustraccionMateria(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/sustraccion-materia`, data);
  }

  declararDesistimiento(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/desistimiento`, data);
  }

  declararNoPresentado(id: string, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/no-presentado`, data);
  }

  notificarSegundaCalificacion(id: string): Observable<any> {
    return this.http.post(`/api/ttaip/segunda-calificacion/${id}/notificar`, {});
  }
}
