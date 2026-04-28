import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TtaipSegundaCalificacionService {

  private apiUrl = 'http://localhost:8080/api/ttaip/segunda-calificacion';

  constructor(private http: HttpClient) { }

  admitirSegundaCalificacion(expediente: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${expediente}/admitir`, {});
  }

  rechazarSegundaCalificacion(expediente: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${expediente}/rechazar`, {});
  }

  notificar(expediente: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${expediente}/notificar`, {});
  }

  declararTenerPorNoPresentado(expediente: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${expediente}/no-presentado`, {});
  }
}
