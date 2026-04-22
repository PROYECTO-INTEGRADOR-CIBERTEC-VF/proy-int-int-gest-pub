import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TtaipService } from './ttaip.service';
// Implementación de pruebas FE-04
describe('TtaipService', () => {
  let service: TtaipService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8080/api/ttaip';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TtaipService]
    });

    service = TestBed.inject(TtaipService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deberia ser creado', () => {
    expect(service).toBeTruthy();
  });

  it('deberia obtener estadisticas', () => {
    service.getEstadisticas().subscribe((response) => {
      expect(response['pendientesAdmision']).toBe(3);
      expect(response['resueltas']).toBe(5);
    });

    const req = httpMock.expectOne(`${apiUrl}/estadisticas`);
    expect(req.request.method).toBe('GET');
    req.flush({ pendientesAdmision: 3, enProceso: 2, enSubsanacion: 1, resueltas: 5 });
  });

  it('deberia listar pendientes', () => {
    service.getPendientes().subscribe((response) => {
      expect(response.length).toBe(2);
    });

    const req = httpMock.expectOne(`${apiUrl}/pendientes`);
    expect(req.request.method).toBe('GET');
    req.flush([{ idApelacion: 1 }, { idApelacion: 2 }]);
  });

  it('deberia listar en analisis', () => {
    service.getEnAnalisis().subscribe();

    const req = httpMock.expectOne(`${apiUrl}/en-calificacion`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('deberia listar en subsanacion', () => {
    service.getSubsanacion().subscribe();

    const req = httpMock.expectOne(`${apiUrl}/subsanacion`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('deberia listar segunda calificacion', () => {
    service.getSegundaCalificacion().subscribe();

    const req = httpMock.expectOne(`${apiUrl}/segunda-calificacion`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('deberia listar resueltas', () => {
    service.getResueltas().subscribe();

    const req = httpMock.expectOne(`${apiUrl}/resueltas`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('deberia admitir apelacion', () => {
    const payload = { fundamentos: 'Cumple requisitos formales' };

    service.admitirApelacion(10, payload).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/calificacion/10/admitir`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });

  it('deberia requerir subsanacion', () => {
    const payload = {
      fundamentos: 'Falta documentacion',
      observaciones: 'Adjuntar sustento',
      diasSubsanacion: 2
    };

    service.requerirSubsanacion(10, payload).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/calificacion/10/subsanar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });

  it('deberia inadmitir apelacion', () => {
    const payload = { fundamentos: 'No corresponde por causal de improcedencia' };

    service.inadmitirApelacion(10, payload).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/calificacion/10/inadmitir`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({});
  });
});
