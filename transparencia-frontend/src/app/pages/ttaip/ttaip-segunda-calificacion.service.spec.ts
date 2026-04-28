import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TtaipSegundaCalificacionService } from './ttaip-segunda-calificacion.service';

describe('TtaipSegundaCalificacionService', () => {
  let service: TtaipSegundaCalificacionService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/ttaip/segunda-calificacion';

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
    const expediente = 'EXP-2026-001';

    // 1. Llamada al método del servicio
    service.admitirSegundaCalificacion(expediente).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    // 2. Comprobar que el servicio intentó ir a la URL correcta usando POST
    const req = httpMock.expectOne(`${apiUrl}/${expediente}/admitir`);
    expect(req.request.method).toBe('POST');

    // 3. Simular que el backend respondió exitosamente
    req.flush(mockResponse);
  });

  it('debería rechazar segunda calificación', () => {
    const mockResponse = { mensaje: 'Rechazado correctamente' };
    const expediente = 'EXP-2026-001';

    service.rechazarSegundaCalificacion(expediente).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${expediente}/rechazar`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('debería notificar', () => {
    const mockResponse = { mensaje: 'Notificado correctamente' };
    const expediente = 'EXP-2026-001';

    service.notificar(expediente).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${expediente}/notificar`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('debería declarar tener por no presentado', () => {
    const mockResponse = { mensaje: 'Declarado como no presentado' };
    const expediente = 'EXP-2026-001';

    service.declararTenerPorNoPresentado(expediente).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/${expediente}/no-presentado`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });
});
