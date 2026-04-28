import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SegundaCalificacionRequest {
  decision: string;
  fundamentos: string;
}

@Injectable({
  providedIn: 'root'
})
export class TtaipSegundaCalificacionService {
  private readonly http = inject(HttpClient);
  // endpoint de la HU-07
  private readonly apiUrl = 'http://localhost:8080/api/ttaip/segunda-calificacion';

  notificarSegundaCalificacion(apelacionId: number, request: SegundaCalificacionRequest, archivo: File): Observable<any> {
    const formData = new FormData();

    // Convertir el JSON a Blob para que Spring Boot lo lea como @RequestPart("datos")
    formData.append('datos', new Blob([JSON.stringify(request)], {
      type: "application/json"
    }));

    // Adjuntar el archivo físico
    formData.append('archivo', archivo, archivo.name);

    return this.http.post(`${this.apiUrl}/${apelacionId}/notificar`, formData);
  }
}
