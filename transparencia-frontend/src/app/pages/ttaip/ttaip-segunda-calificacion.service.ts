import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface CalificacionRequest {
  fundamentos: string;
  miembroId?: number;
  observaciones?: string;
  diasSubsanacion?: number;
}

@Injectable({
  providedIn: 'root'
})
export class TtaipSegundaCalificacionService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/ttaip/calificacion';

  admitirSegundaCalificacion(apelacionId: number, data: CalificacionRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${apelacionId}/admitir`, data);
  }

  rechazarSegundaCalificacion(apelacionId: number, data: CalificacionRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${apelacionId}/inadmitir`, data);
  }

  declararTenerPorNoPresentado(apelacionId: number, data: CalificacionRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/${apelacionId}/no-presentado`, data);
  }
}
