import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TtaipSegundaCalificacionService } from './ttaip-segunda-calificacion.service';

describe('TtaipSegundaCalificacionService', () => {
  let service: TtaipSegundaCalificacionService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TtaipSegundaCalificacionService]
    });
    service = TestBed.inject(TtaipSegundaCalificacionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verificacion para que peticiones no se queden colgadas después de cada prueba
    httpMock.verify();
  });

  it('debería ser creado', () => {
    expect(service).toBeTruthy();
  });

  it('debería notificar segunda calificación con archivo', () => {
    const mockResponse = { id: 25, estado: 'NOTIFICACION_SEGUNDA_CALIFICACION' };
    const apelacionId = 25;
    const requestData = { decision: 'ADMITIR', fundamentos: 'Fundamentos de prueba' };

    // Simular un archivo PDF
    const mockFile = new File([''], 'resolucion.pdf', { type: 'application/pdf' });

    // Llamar al método nuevo creado
    service.notificarSegundaCalificacion(apelacionId, requestData, mockFile).subscribe((res: any) => {
      expect(res).toEqual(mockResponse);
    });

    // Validar que se haga una petición POST
    const req = httpMock.expectOne(req => req.method === 'POST');
    expect(req.request.method).toBe('POST');

    // el backend responde exitosamente
    req.flush(mockResponse);
  });
});
