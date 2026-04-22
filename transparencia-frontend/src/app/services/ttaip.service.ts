import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TtaipService {
  private baseUrl = '/api/ttaip/resolucion';

  constructor(private http: HttpClient) { }

  // Métodos de Resolución Final
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

  // Otros métodos
  notificarSegundaCalificacion(id: string): Observable<any> {
    return this.http.post(`/api/ttaip/segunda-calificacion/${id}/notificar`, {});
  }
}
