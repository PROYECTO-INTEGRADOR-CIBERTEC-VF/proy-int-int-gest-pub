import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApelacionService } from './apelacion.service';
import { Apelacion, ApelacionCreate, EstadoApelacion } from '../models/apelacion.model';

describe('ApelacionService', () => {
  const apiUrl = 'http://localhost:8080/api/apelaciones';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApelacionService]
    });
  });

  afterEach(() => {
    const httpMock = TestBed.inject(HttpTestingController);
    httpMock.verify();
  });

  it('debería ser creado', () => {
    const service = TestBed.inject(ApelacionService);
    expect(service).toBeTruthy();
  });

  it('debería listar las apelaciones', () => {
    const service = TestBed.inject(ApelacionService);
    const httpMock = TestBed.inject(HttpTestingController);
    
    const mockApelaciones: Apelacion[] = [
      { idApelacion: 1, expediente: '00001-2026-JUS-TTAIP', solicitudId: 1, ciudadanoId: 1, fundamentos: 'Test', fechaApelacion: '2026-04-16', estado: EstadoApelacion.PENDIENTE_ELEVACION }
    ];

    service.findAll().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res).toEqual(mockApelaciones);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockApelaciones);
  });

  it('debería crear una apelación', () => {
    const service = TestBed.inject(ApelacionService);
    const httpMock = TestBed.inject(HttpTestingController);
    
    const nuevaApelacion: ApelacionCreate = { solicitudId: 1, ciudadanoId: 1, fundamentos: 'Nuevos fundamentos' };
    const mockRespuesta: Apelacion = { idApelacion: 2, expediente: '00002-2026-JUS-TTAIP', ...nuevaApelacion, fechaApelacion: '2026-04-16', estado: EstadoApelacion.PENDIENTE_ELEVACION };

    service.crear(nuevaApelacion).subscribe(res => {
      expect(res.idApelacion).toBe(2);
      expect(res.expediente).toBe('00002-2026-JUS-TTAIP');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevaApelacion);
    req.flush(mockRespuesta);
  });

  it('debería buscar por expediente', () => {
    const service = TestBed.inject(ApelacionService);
    const httpMock = TestBed.inject(HttpTestingController);
    
    const expediente = '00001-2026-JUS-TTAIP';
    const mockRespuesta: Apelacion = { idApelacion: 1, expediente: expediente, solicitudId: 1, ciudadanoId: 1, fundamentos: 'Test', fechaApelacion: '2026-04-16', estado: EstadoApelacion.PENDIENTE_ELEVACION };

    service.findByExpediente(expediente).subscribe(res => {
      expect(res.expediente).toBe(expediente);
    });

    const req = httpMock.expectOne(`${apiUrl}/expediente/${expediente}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockRespuesta);
  });

  it('debería buscar por ciudadano', () => {
    const service = TestBed.inject(ApelacionService);
    const httpMock = TestBed.inject(HttpTestingController);
    
    const ciudadanoId = 5;
    const mockRespuesta: Apelacion[] = [
      { idApelacion: 1, expediente: '00001-2026-JUS-TTAIP', solicitudId: 1, ciudadanoId: ciudadanoId, fundamentos: 'Test', fechaApelacion: '2026-04-16', estado: EstadoApelacion.PENDIENTE_ELEVACION }
    ];

    service.findByCiudadanoId(ciudadanoId).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].ciudadanoId).toBe(ciudadanoId);
    });

    const req = httpMock.expectOne(`${apiUrl}/ciudadano/${ciudadanoId}`);
    expect(req.request.method).toBe('GET');
    req.flush(mockRespuesta);
  });

  it('debería cambiar el estado de la apelación', () => {
    const service = TestBed.inject(ApelacionService);
    const httpMock = TestBed.inject(HttpTestingController);
    
    const id = 1;
    const nuevoEstado = EstadoApelacion.EN_CALIFICACION_1;
    const mockRespuesta: Apelacion = { idApelacion: id, expediente: '00001-2026-JUS-TTAIP', solicitudId: 1, ciudadanoId: 1, fundamentos: 'Test', fechaApelacion: '2026-04-16', estado: nuevoEstado };

    service.cambiarEstado(id, nuevoEstado).subscribe(res => {
      expect(res.estado).toBe(nuevoEstado);
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}/estado`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ estado: nuevoEstado });
    req.flush(mockRespuesta);
  });

  it('debería subsanar la apelación', () => {
    const service = TestBed.inject(ApelacionService);
    const httpMock = TestBed.inject(HttpTestingController);
    
    const id = 1;
    const subsanacion = 'Fundamentos corregidos';
    const mockRespuesta: Apelacion = { idApelacion: id, expediente: '00001-2026-JUS-TTAIP', solicitudId: 1, ciudadanoId: 1, fundamentos: 'Test\n\n--- SUBSANACIÓN ---\nFundamentos corregidos', fechaApelacion: '2026-04-16', estado: EstadoApelacion.EN_CALIFICACION_2 };

    service.subsanar(id, subsanacion).subscribe(res => {
      expect(res.fundamentos).toContain('SUBSANACIÓN');
      expect(res.estado).toBe(EstadoApelacion.EN_CALIFICACION_2);
    });

    const req = httpMock.expectOne(`${apiUrl}/${id}/subsanacion`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ fundamentosAdicionales: subsanacion });
    req.flush(mockRespuesta);
  });
});