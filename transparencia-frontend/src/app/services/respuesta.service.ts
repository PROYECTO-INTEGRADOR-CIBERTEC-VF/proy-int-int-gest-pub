import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearRespuestaRequest, Respuesta } from '../models/respuesta.model';

@Injectable({ providedIn: 'root' })
export class RespuestaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/respuestas';

  findAll(): Observable<Respuesta[]> {
    return this.http.get<Respuesta[]>(this.apiUrl);
  }

  findById(id: number): Observable<Respuesta> {
    return this.http.get<Respuesta>(`${this.apiUrl}/${id}`);
  }

  findBySolicitudId(solicitudId: number): Observable<Respuesta> {
    return this.http.get<Respuesta>(`${this.apiUrl}/solicitud/${solicitudId}`);
  }

  aceptarSolicitud(data: CrearRespuestaRequest): Observable<Respuesta> {
    return this.http.post<Respuesta>(`${this.apiUrl}/aceptar`, data);
  }

  denegarSolicitud(data: CrearRespuestaRequest): Observable<Respuesta> {
    return this.http.post<Respuesta>(`${this.apiUrl}/denegar`, data);
  }

  getCausalesDenegacion(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/causales-denegacion`);
  }
}
