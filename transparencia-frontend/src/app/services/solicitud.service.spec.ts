import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SolicitudService } from './solicitud.service';
import { EstadoSolicitud, Solicitud, SolicitudCreate } from '../models/solicitud.model';

describe('SolicitudService', () => {
  let service: SolicitudService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/solicitudes';

  const solicitudMock: Solicitud = {
    idSolicitud: 1,
    expediente: 'SAIP-2026-00001',
    asunto: 'Acceso a informacion',
    descripcion: 'Detalle de la solicitud',
    estado: 'RECEPCIONADA',
    ciudadanoId: 15,
    entidadId: 3,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(SolicitudService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deberia crear el servicio', () => {
    expect(service).toBeTruthy();
  });

  it('deberia obtener todas las solicitudes con GET /api/solicitudes', () => {
    service.findAll().subscribe((solicitudes) => {
      expect(solicitudes).toEqual([solicitudMock]);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([solicitudMock]);
  });

  it('deberia obtener una solicitud por id con GET /api/solicitudes/{id}', () => {
    service.findById(1).subscribe((solicitud) => {
      expect(solicitud).toEqual(solicitudMock);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(solicitudMock);
  });

  it('deberia obtener por expediente con GET /api/solicitudes/expediente/{expediente}', () => {
    const expediente = 'SAIP-2026-00001';

    service.findByExpediente(expediente).subscribe((solicitud) => {
      expect(solicitud.expediente).toBe(expediente);
    });

    const req = httpMock.expectOne(`${apiUrl}/expediente/${expediente}`);
    expect(req.request.method).toBe('GET');
    req.flush(solicitudMock);
  });

  it('deberia listar por ciudadano con GET /api/solicitudes/ciudadano/{ciudadanoId}', () => {
    service.findByCiudadanoId(15).subscribe((solicitudes) => {
      expect(solicitudes).toEqual([solicitudMock]);
    });

    const req = httpMock.expectOne(`${apiUrl}/ciudadano/15`);
    expect(req.request.method).toBe('GET');
    req.flush([solicitudMock]);
  });

  it('deberia listar por entidad con GET /api/solicitudes/entidad/{entidadId}', () => {
    service.findByEntidadId(3).subscribe((solicitudes) => {
      expect(solicitudes).toEqual([solicitudMock]);
    });

    const req = httpMock.expectOne(`${apiUrl}/entidad/3`);
    expect(req.request.method).toBe('GET');
    req.flush([solicitudMock]);
  });

  it('deberia listar por estado con GET /api/solicitudes/estado/{estado}', () => {
    const estado: EstadoSolicitud = 'RECEPCIONADA';

    service.findByEstado(estado).subscribe((solicitudes) => {
      expect(solicitudes).toEqual([solicitudMock]);
    });

    const req = httpMock.expectOne(`${apiUrl}/estado/${estado}`);
    expect(req.request.method).toBe('GET');
    req.flush([solicitudMock]);
  });

  it('deberia obtener estadisticas globales con GET /api/solicitudes/estadisticas', () => {
    const estadisticas = { RECEPCIONADA: 4, RESPONDIDA: 2 };

    service.getEstadisticas().subscribe((response) => {
      expect(response).toEqual(estadisticas);
    });

    const req = httpMock.expectOne(`${apiUrl}/estadisticas`);
    expect(req.request.method).toBe('GET');
    req.flush(estadisticas);
  });

  it('deberia obtener estadisticas de ciudadano con GET /api/solicitudes/ciudadano/{id}/estadisticas', () => {
    const estadisticas = { RECEPCIONADA: 1, VENCIDA: 1 };

    service.getEstadisticasCiudadano(15).subscribe((response) => {
      expect(response).toEqual(estadisticas);
    });

    const req = httpMock.expectOne(`${apiUrl}/ciudadano/15/estadisticas`);
    expect(req.request.method).toBe('GET');
    req.flush(estadisticas);
  });

  it('deberia obtener estadisticas de entidad con GET /api/solicitudes/entidad/{id}/estadisticas', () => {
    const estadisticas = { RECEPCIONADA: 6, DENEGADA: 1 };

    service.getEstadisticasEntidad(3).subscribe((response) => {
      expect(response).toEqual(estadisticas);
    });

    const req = httpMock.expectOne(`${apiUrl}/entidad/3/estadisticas`);
    expect(req.request.method).toBe('GET');
    req.flush(estadisticas);
  });

  it('deberia crear solicitud con POST /api/solicitudes', () => {
    const nuevaSolicitud: SolicitudCreate = {
      asunto: 'Nueva solicitud',
      descripcion: 'Descripcion de prueba',
      ciudadanoId: 15,
      entidadId: 3,
      tipoInformacion: 'Documento',
      formatoDigital: true,
      formatoFisico: false,
      copiaSimple: true,
      copiaCertificada: false,
    };

    service.crear(nuevaSolicitud).subscribe((solicitud) => {
      expect(solicitud).toEqual(solicitudMock);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevaSolicitud);
    req.flush(solicitudMock);
  });

  it('deberia actualizar solicitud con PUT /api/solicitudes/{id}', () => {
    const payload: Partial<Solicitud> = { asunto: 'Asunto actualizado' };

    service.actualizar(1, payload).subscribe((solicitud) => {
      expect(solicitud.asunto).toBe('Asunto actualizado');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush({ ...solicitudMock, asunto: 'Asunto actualizado' });
  });

  it('deberia cambiar estado con PATCH /api/solicitudes/{id}/estado', () => {
    const nuevoEstado: EstadoSolicitud = 'EN_REVISION';

    service.cambiarEstado(1, nuevoEstado).subscribe((solicitud) => {
      expect(solicitud.estado).toBe(nuevoEstado);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/estado`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ estado: nuevoEstado });
    req.flush({ ...solicitudMock, estado: nuevoEstado });
  });

  it('deberia eliminar solicitud con DELETE /api/solicitudes/{id}', () => {
    service.eliminar(1).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
