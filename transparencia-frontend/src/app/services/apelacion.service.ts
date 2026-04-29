import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apelacion } from '../models/apelacion.model';

@Injectable({
  providedIn: 'root'
})
export class ApelacionService {
  
  private apiUrl = 'http://localhost:8080/api/apelaciones'; 

  constructor(private http: HttpClient) {}

  subsanar(id: number, fundamentosAdicionales: string): Observable<Apelacion> {
    const payload = {
      fundamentosAdicionales: fundamentosAdicionales
    };
    
    return this.http.post<Apelacion>(`${this.apiUrl}/${id}/subsanacion`, payload);
  }
}