import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TtaipService } from './ttaip.service';

describe('TtaipService - Calificación Final (FE-03)', () => {
  let service: TtaipService;
  let httpMock: HttpTestingController;

  const mockExpediente = 'EXP-2026-TEST';
  const mockData = {
    decision: '',
    fundamentos: 'Estos son fundamentos de prueba obligatorios',
    iniciarProcesoDisciplinario: false
  };

  const baseUrl = '/api/ttaip/resolucion';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TtaipService]
    });
    service = TestBed.inject(TtaipService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verifica que no haya peticiones HTTP pendientes
    httpMock.verify();
  });

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
