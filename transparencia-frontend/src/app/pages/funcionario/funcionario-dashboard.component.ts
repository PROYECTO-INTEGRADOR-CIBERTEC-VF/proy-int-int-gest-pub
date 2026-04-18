import {
  ChangeDetectionStrategy,
  Component,
  PLATFORM_ID,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe, isPlatformBrowser } from '@angular/common';
import { Solicitud } from '../../models/solicitud.model';
import { SolicitudService } from '../../services/solicitud.service';

interface SesionFuncionario {
  entidadId?: number;
  entidadNombre?: string;
}

@Component({
  selector: 'app-funcionario-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe],
  templateUrl: './funcionario-dashboard.component.html',
  styleUrl: './funcionario-dashboard.component.css',
})
export class FuncionarioDashboardComponent {
  private readonly solicitudService = inject(SolicitudService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly entidadId = signal<number | null>(null);
  readonly entidadNombre = signal('Entidad publica');
  readonly solicitudes = signal<Solicitud[]>([]);

  readonly pendientes = computed(() => this.solicitudes().filter((s) => this.canResponder(s)).length);

  readonly vencidas = computed(() => this.solicitudes().filter((s) => this.esSilencioAdministrativo(s)).length);

  readonly respondidas = computed(() => this.solicitudes().filter((s) => this.tieneRespuesta(s)).length);

  readonly urgentes = computed(() =>
    this.solicitudes().filter((s) => {
      const dias = this.obtenerDiasRestantes(s);
      return dias !== null && dias >= 0 && dias <= 1 && this.canResponder(s);
    }).length,
  );

  constructor() {
    this.inicializarDesdeSesion();
  }

  cargarSolicitudes(): void {
    const idEntidad = this.entidadId();
    if (!idEntidad) {
      this.loading.set(false);
      this.error.set('No se encontro entidad asociada al funcionario en sesion.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.solicitudService.findByEntidadId(idEntidad).subscribe({
      next: (data) => {
        this.solicitudes.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar las solicitudes de la entidad.');
      },
    });
  }

  canResponder(solicitud: Solicitud): boolean {
    return !this.esSilencioAdministrativo(solicitud) && !this.tieneRespuesta(solicitud);
  }

  obtenerSemaforoEtiqueta(solicitud: Solicitud): string {
    const dias = this.obtenerDiasRestantes(solicitud);
    if (this.esSilencioAdministrativo(solicitud)) {
      return 'NEGRO';
    }
    if (dias === null) {
      return 'N/A';
    }
    if (dias >= 5) {
      return 'VERDE';
    }
    if (dias >= 2) {
      return 'AMBAR';
    }
    if (dias >= 0) {
      return 'ROJO';
    }
    return 'NEGRO';
  }

  obtenerSemaforoClase(solicitud: Solicitud): string {
    const etiqueta = this.obtenerSemaforoEtiqueta(solicitud);
    if (etiqueta === 'VERDE') {
      return 'bg-emerald-100 text-emerald-700 border border-emerald-200';
    }
    if (etiqueta === 'AMBAR') {
      return 'bg-amber-100 text-amber-700 border border-amber-200';
    }
    if (etiqueta === 'ROJO') {
      return 'bg-red-100 text-red-700 border border-red-200';
    }
    if (etiqueta === 'NEGRO') {
      return 'bg-neutral-800 text-white border border-neutral-700';
    }
    return 'bg-neutral-100 text-neutral-700 border border-neutral-200';
  }

  obtenerDiasRestantes(solicitud: Solicitud): number | null {
    return typeof solicitud.diasRestantes === 'number' ? solicitud.diasRestantes : null;
  }

  private inicializarDesdeSesion(): void {
    if (!isPlatformBrowser(this.platformId)) {
      this.loading.set(false);
      return;
    }

    const rawSesion = localStorage.getItem('usuario');
    if (!rawSesion) {
      this.loading.set(false);
      this.error.set('No hay sesion activa de funcionario.');
      return;
    }

    try {
      const sesion = JSON.parse(rawSesion) as SesionFuncionario;
      if (typeof sesion.entidadId !== 'number' || sesion.entidadId <= 0) {
        this.loading.set(false);
        this.error.set('La sesion no contiene una entidad valida para el funcionario.');
        return;
      }

      this.entidadId.set(sesion.entidadId);
      this.entidadNombre.set(sesion.entidadNombre ?? 'Entidad publica');
      this.cargarSolicitudes();
    } catch {
      this.loading.set(false);
      this.error.set('No se pudo leer la sesion de usuario.');
    }
  }

  private tieneRespuesta(solicitud: Solicitud): boolean {
    return solicitud.estado === 'RESPONDIDA' || solicitud.estado === 'DENEGADA' || solicitud.estado === 'CONCLUIDA';
  }

  private esSilencioAdministrativo(solicitud: Solicitud): boolean {
    const dias = this.obtenerDiasRestantes(solicitud);
    const porEstado = solicitud.estado === 'VENCIDA';
    const porDias = dias !== null && dias < 0;
    const porSemaforo = solicitud.semaforo === 'NEGRO';
    return porEstado || porDias || porSemaforo;
  }
}