import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Solicitud } from '../models/solicitud.model';

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private apiUrl = 'http://localhost:8080/api/solicitudes';

  constructor(private http: HttpClient) {}

  findByExpediente(expediente: string): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.apiUrl}/expediente/${expediente}`);
  }

  findById(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.apiUrl}/${id}`);
  }

  findByCiudadanoId(ciudadanoId: number): Observable<Solicitud[]> {
    return this.http.get<Solicitud[]>(`${this.apiUrl}/ciudadano/${ciudadanoId}`);
  }
}
