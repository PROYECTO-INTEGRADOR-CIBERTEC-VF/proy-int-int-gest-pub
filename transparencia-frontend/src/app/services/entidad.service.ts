import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Entidad } from '../models/entidad.model';

@Injectable({ providedIn: 'root' })
export class EntidadService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/entidades';

  findAll(): Observable<Entidad[]> {
    return this.http.get<Entidad[]>(this.apiUrl);
  }

  findById(id: number): Observable<Entidad> {
    return this.http.get<Entidad>(`${this.apiUrl}/${id}`);
  }
}
