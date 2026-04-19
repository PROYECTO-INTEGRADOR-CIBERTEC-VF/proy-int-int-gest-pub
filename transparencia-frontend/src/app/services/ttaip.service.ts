import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TtaipService {

  constructor() { }

  // Métodos
  declararFundado(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado FUNDADO.` }).pipe(delay(800));
  }

  declararInfundado(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado INFUNDADO.` }).pipe(delay(800));
  }

  declararFundadoEnParte(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado FUNDADO EN PARTE.` }).pipe(delay(800));
  }

  declararInfundadoEnParte(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado INFUNDADO EN PARTE.` }).pipe(delay(800));
  }

  declararImprocedente(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado IMPROCEDENTE.` }).pipe(delay(800));
  }

  declararSustraccionMateria(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} concluido por SUSTRACCIÓN DE MATERIA.` }).pipe(delay(800));
  }

  declararDesistimiento(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} concluido por DESISTIMIENTO.` }).pipe(delay(800));
  }

  declararNoPresentado(id: string, data: any): Observable<any> {
    return of({ status: 'success', mensaje: `Expediente ${id} declarado NO PRESENTADO.` }).pipe(delay(800));
  }

  notificarSegundaCalificacion(id: string): Observable<any> {
    return of({ status: 'success', mensaje: `Notificación enviada para expediente ${id}.` }).pipe(delay(800));
  }
}
