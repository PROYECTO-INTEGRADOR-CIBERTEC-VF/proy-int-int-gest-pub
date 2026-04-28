import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apelacion, ApelacionCreate, EstadoApelacion } from '../models/apelacion.model';

@Injectable({
    providedIn: 'root'
})
export class ApelacionService {

    private apiUrl = 'http://localhost:8080/api/apelaciones';

    constructor(private http: HttpClient) { }

    findAll(): Observable<Apelacion[]> {
        return this.http.get<Apelacion[]>(this.apiUrl);
    }

    findById(id: number): Observable<Apelacion> {
        return this.http.get<Apelacion>(`${this.apiUrl}/${id}`);
    }

    findByExpediente(expediente: string): Observable<Apelacion> {
        return this.http.get<Apelacion>(`${this.apiUrl}/expediente/${expediente}`);
    }

    findByEstado(estado: EstadoApelacion): Observable<Apelacion[]> {
        return this.http.get<Apelacion[]>(`${this.apiUrl}/estado/${estado}`);
    }

    findByCiudadanoId(ciudadanoId: number): Observable<Apelacion[]> {
        return this.http.get<Apelacion[]>(`${this.apiUrl}/ciudadano/${ciudadanoId}`);
    }

    getEstadisticas(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/estadisticas`);
    }

    crear(data: ApelacionCreate): Observable<Apelacion> {
        return this.http.post<Apelacion>(this.apiUrl, data);
    }

    actualizar(id: number, data: Partial<Apelacion>): Observable<Apelacion> {
        return this.http.put<Apelacion>(`${this.apiUrl}/${id}`, data);
    }

    cambiarEstado(id: number, estado: EstadoApelacion): Observable<Apelacion> {
        return this.http.patch<Apelacion>(`${this.apiUrl}/${id}/estado`, { estado });
    }

    subsanar(id: number, fundamentosAdicionales: string): Observable<Apelacion> {
        return this.http.post<Apelacion>(`${this.apiUrl}/${id}/subsanacion`, { fundamentosAdicionales });
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}