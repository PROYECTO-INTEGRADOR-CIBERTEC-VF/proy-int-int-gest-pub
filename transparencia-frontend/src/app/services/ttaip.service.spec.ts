import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TtaipService } from './ttaip.service';

describe('TtaipService', () => {
  let service: TtaipService;
  let httpMock: HttpTestingController;

  // Variables de las pruebas (HU-08)
  const mockExpediente = 'EXP-2026-TEST';
  const mockData = {
    decision: '',
    fundamentos: 'Estos son fundamentos de prueba obligatorios',
    iniciarProcesoDisciplinario: false
  };
  const baseUrl = '/api/ttaip/resolucion';

  // Variables de pruebas de develop
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


  // PRUEBAS DE DEVELOP DEL EQUIPO

  it('deberia ser creado', () => {
    expect(service).toBeTruthy();
  });

  it('deberia obtener estadisticas', () => {
    service.getEstadisticas().subscribe((response: any) => {
      expect(response['pendientesAdmision']).toBe(3);
      expect(response['resueltas']).toBe(5);
    });

    const req = httpMock.expectOne(`${apiUrl}/estadisticas`);
    expect(req.request.method).toBe('GET');
    req.flush({ pendientesAdmision: 3, enProceso: 2, enSubsanacion: 1, resueltas: 5 });
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


  // PRUEBAS DE RESOLUCIÓN FINAL (HU-08)

  it('debe enviar la decisión FUNDADO correctamente', () => {
    mockData.decision = 'FUNDADO';
    service.declararFundado(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/fundado`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockData);
    req.flush({ success: true });
  });

  it('debe enviar la decisión INFUNDADO correctamente', () => {
    mockData.decision = 'INFUNDADO';
    service.declararInfundado(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/infundado`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('debe enviar la decisión FUNDADO EN PARTE correctamente', () => {
    mockData.decision = 'FUNDADO_PARTE';
    mockData.iniciarProcesoDisciplinario = true;
    service.declararFundadoEnParte(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/fundado-en-parte`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.iniciarProcesoDisciplinario).toBe(true);
    req.flush({ success: true });
  });

  it('debe enviar la decisión INFUNDADO EN PARTE correctamente', () => {
    mockData.decision = 'INFUNDADO_PARTE';
    service.declararInfundadoEnParte(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/infundado-en-parte`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('debe enviar la decisión IMPROCEDENTE correctamente', () => {
    mockData.decision = 'IMPROCEDENTE';
    service.declararImprocedente(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/improcedente`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('debe enviar la decisión CONCLUSIÓN POR SUSTRACCIÓN DE MATERIA correctamente', () => {
    mockData.decision = 'SUSTRACCION';
    service.declararSustraccionMateria(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/sustraccion-materia`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('debe enviar la decisión CONCLUSIÓN POR DESISTIMIENTO correctamente', () => {
    mockData.decision = 'DESISTIMIENTO';
    service.declararDesistimiento(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/desistimiento`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });

  it('debe enviar la decisión NO PRESENTADO correctamente', () => {
    mockData.decision = 'NO_PRESENTADO';
    service.declararNoPresentado(mockExpediente, mockData).subscribe(res => {
      expect(res).toBeTruthy();
    });
    const req = httpMock.expectOne(`${baseUrl}/${mockExpediente}/no-presentado`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });
  });
});
