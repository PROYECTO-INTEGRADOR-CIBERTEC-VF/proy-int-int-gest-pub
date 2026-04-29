import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApelacionService } from './apelacion.service';
import { Apelacion } from '../models/apelacion.model';

describe('ApelacionService - Subsanacion', () => {
  let service: ApelacionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApelacionService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ApelacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deberia enviar la peticion al servidor con el identificador correcto', () => {
    const idMock = 1;
    const fundamentosMock = 'Nuevos fundamentos';
    const dummyResponse = { idApelacion: 1, estado: 'EN_CALIFICACION_2' } as Apelacion;

    service.subsanar(idMock, fundamentosMock).subscribe(res => {
      expect(res).toEqual(dummyResponse);
    });

    const req = httpMock.expectOne(`http://localhost:8080/api/apelaciones/${idMock}/subsanacion`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ fundamentosAdicionales: fundamentosMock });
    req.flush(dummyResponse);
  });

  it('deberia mostrar error cuando la subsanacion es rechazada por plazo vencido', () => {
    const idMock = 1;
    const fundamentosMock = 'Nuevos fundamentos';

    service.subsanar(idMock, fundamentosMock).subscribe({
      next: () => fail('Deberia haber fallado por plazo vencido'),
      error: (err) => {
        expect(err.status).toBe(400);
        expect(err.error).toBe('El plazo para subsanar ha vencido');
      }
    });

    const req = httpMock.expectOne(`http://localhost:8080/api/apelaciones/${idMock}/subsanacion`);
    req.flush('El plazo para subsanar ha vencido', { status: 400, statusText: 'Bad Request' });
  });
});