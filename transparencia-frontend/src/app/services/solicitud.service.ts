import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  EstadoSolicitud,
  Solicitud,
  SolicitudCreate,
} from '../models/solicitud.model';

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/solicitudes';

  findAll(): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(this.apiUrl);
  }

  findById(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.apiUrl}/${id}`);
  }

  findByExpediente(expediente: string): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.apiUrl}/expediente/${expediente}`);
  }

  findByCiudadanoId(ciudadanoId: number): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${this.apiUrl}/ciudadano/${ciudadanoId}`);
  }

  findByEntidadId(entidadId: number): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${this.apiUrl}/entidad/${entidadId}`);
  }

  findByEstado(estado: EstadoSolicitud): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${this.apiUrl}/estado/${estado}`);
  }

  getEstadisticas(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/estadisticas`);
  }

  getEstadisticasCiudadano(ciudadanoId: number): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/ciudadano/${ciudadanoId}/estadisticas`);
  }

  getEstadisticasEntidad(entidadId: number): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.apiUrl}/entidad/${entidadId}/estadisticas`);
  }

  crear(solicitud: SolicitudCreate): Observable<Solicitud> {
    return this.http.post<Solicitud>(this.apiUrl, solicitud);
  }

  actualizar(id: number, solicitud: Partial<Solicitud>): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.apiUrl}/${id}`, solicitud);
  }

  cambiarEstado(id: number, estado: EstadoSolicitud): Observable<Solicitud> {
    return this.http.patch<Solicitud>(`${this.apiUrl}/${id}/estado`, { estado });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
