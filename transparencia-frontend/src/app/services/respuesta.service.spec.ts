import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { RespuestaService } from './respuesta.service';
import { CrearRespuestaRequest } from '../models/respuesta.model';

describe('RespuestaService', () => {
  let service: RespuestaService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/respuestas';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(RespuestaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deberia crear el servicio', () => {
    expect(service).toBeTruthy();
  });

  it('deberia obtener todas las respuestas con GET /api/respuestas', () => {
    service.findAll().subscribe((respuestas) => {
      expect(respuestas.length).toBe(1);
      expect(respuestas[0]?.id).toBe(10);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 10, tipoRespuesta: 'ENTREGA_TOTAL' }]);
  });

  it('deberia obtener una respuesta por id con GET /api/respuestas/{id}', () => {
    service.findById(4).subscribe((respuesta) => {
      expect(respuesta.id).toBe(4);
    });

    const req = httpMock.expectOne(`${apiUrl}/4`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 4 });
  });

  it('deberia obtener una respuesta por solicitud con GET /api/respuestas/solicitud/{id}', () => {
    service.findBySolicitudId(8).subscribe((respuesta) => {
      expect(respuesta.id).toBe(11);
    });

    const req = httpMock.expectOne(`${apiUrl}/solicitud/8`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 11 });
  });

  it('deberia aceptar solicitud con POST /api/respuestas/aceptar', () => {
    const payload: CrearRespuestaRequest = {
      solicitudId: 1,
      funcionarioId: 2,
      tipoRespuesta: 'ENTREGA_TOTAL',
      contenido: 'Se entrega la informacion solicitada',
      formatoEntrega: 'DIGITAL',
      plazoEntrega: 5,
    };

    service.aceptarSolicitud(payload).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/aceptar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 12, tipoRespuesta: 'ENTREGA_TOTAL' });
  });

  it('deberia denegar solicitud con POST /api/respuestas/denegar', () => {
    const payload: CrearRespuestaRequest = {
      solicitudId: 1,
      funcionarioId: 2,
      tipoRespuesta: 'DENEGACION_TOTAL',
      contenido: 'No procede por causal legal',
      causalDenegatoria: 'Informacion reservada',
      fundamentoLegal: 'Art. 15-B Ley 27806',
    };

    service.denegarSolicitud(payload).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/denegar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 13, tipoRespuesta: 'DENEGACION_TOTAL' });
  });

  it('deberia listar causales con GET /api/respuestas/causales-denegacion', () => {
    const causalesEsperadas = [
      'Informacion Confidencial - Art. 15 Ley 27806',
      'Informacion Secreta - Art. 15-A Ley 27806',
    ];

    service.getCausalesDenegacion().subscribe((causales) => {
      expect(causales).toEqual(causalesEsperadas);
    });

    const req = httpMock.expectOne(`${apiUrl}/causales-denegacion`);
    expect(req.request.method).toBe('GET');
    req.flush(causalesEsperadas);
  });

  it('deberia retornar arreglo vacio cuando el backend no envia causales', () => {
    service.getCausalesDenegacion().subscribe((causales) => {
      expect(causales).toEqual([]);
    });

    const req = httpMock.expectOne(`${apiUrl}/causales-denegacion`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
