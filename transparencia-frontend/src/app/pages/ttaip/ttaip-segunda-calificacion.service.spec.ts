import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TtaipSegundaCalificacionService } from './ttaip-segunda-calificacion.service';

describe('TtaipSegundaCalificacionService', () => {
  let service: TtaipSegundaCalificacionService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/ttaip/calificacion';
  const payload = {
    fundamentos: 'Fundamentos de prueba',
    miembroId: 10
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule], // Simulador de peticiones HTTP
      providers: [TtaipSegundaCalificacionService]
    });
    service = TestBed.inject(TtaipSegundaCalificacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verificamos que no queden peticiones colgadas después de cada prueba
    httpMock.verify();
  });

  it('debería ser creado', () => {
    expect(service).toBeTruthy();
  });

  it('debería admitir segunda calificación', () => {
    const mockResponse = { mensaje: 'Admitido correctamente' };
    const apelacionId = 25;

    service.admitirSegundaCalificacion(apelacionId, payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${apelacionId}/admitir`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('debería rechazar segunda calificación', () => {
    const mockResponse = { mensaje: 'Rechazado correctamente' };
    const apelacionId = 25;

    service.rechazarSegundaCalificacion(apelacionId, payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${apelacionId}/inadmitir`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });

  it('debería declarar tener por no presentado', () => {
    const mockResponse = { mensaje: 'Declarado como no presentado' };
    const apelacionId = 25;

    service.declararTenerPorNoPresentado(apelacionId, payload).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${apelacionId}/no-presentado`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockResponse);
  });
});
