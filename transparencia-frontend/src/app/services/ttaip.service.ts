import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
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
  private readonly apiUrl = 'http://localhost:8080/api/ttaip';

  constructor(private readonly http: HttpClient) { }

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

  getSegundaCalificacion(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/segunda-calificacion`);
  }

  getResueltas(): Observable<Apelacion[]> {
    return this.http.get<Apelacion[]>(`${this.apiUrl}/resueltas`);
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

  // Métodos
  declararFundado(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado FUNDADO.` }).pipe(delay(800));
  }

  declararInfundado(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado INFUNDADO.` }).pipe(delay(800));
  }

  declararFundadoEnParte(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado FUNDADO EN PARTE.` }).pipe(delay(800));
  }

  declararInfundadoEnParte(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado INFUNDADO EN PARTE.` }).pipe(delay(800));
  }

  declararImprocedente(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado IMPROCEDENTE.` }).pipe(delay(800));
  }

  declararSustraccionMateria(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} concluido por SUSTRACCIÓN DE MATERIA.` }).pipe(delay(800));
  }

  declararDesistimiento(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} concluido por DESISTIMIENTO.` }).pipe(delay(800));
  }

  declararNoPresentado(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado NO PRESENTADO.` }).pipe(delay(800));
  }

  notificarSegundaCalificacion(id: string): Observable<any> {
    return of({ status: 'success', mensaje: `Notificación enviada para expediente ${id}.` }).pipe(delay(800));
  }
}
