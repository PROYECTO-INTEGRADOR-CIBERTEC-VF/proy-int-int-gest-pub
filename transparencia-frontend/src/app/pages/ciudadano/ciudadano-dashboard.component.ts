import { ChangeDetectionStrategy, Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { DatePipe, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Solicitud } from '../../models/solicitud.model';
import { Apelacion } from '../../models/apelacion.model';
import { SolicitudService } from '../../services/solicitud.service';
import { ApelacionService } from '../../services/apelacion.service';
import { AuthService } from '../../services/auth.service';

type TabActiva = 'saip' | 'apelaciones';

interface SesionCiudadano {
  ciudadanoId?: number;
  nombre?: string;
}

@Component({
  selector: 'app-ciudadano-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, FormsModule],
  templateUrl: './ciudadano-dashboard.component.html',
  styleUrl: './ciudadano-dashboard.component.css',
})
export class CiudadanoDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly solicitudService = inject(SolicitudService);
  private readonly apelacionService = inject(ApelacionService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly loading = signal(true);
  readonly loadingApelaciones = signal(false);
  readonly error = signal<string | null>(null);
  readonly ciudadanoId = signal<number | null>(null);
  readonly nombreUsuario = signal('Ciudadano');
  readonly solicitudes = signal<Solicitud[]>([]);
  readonly apelaciones = signal<Apelacion[]>([]);
  readonly tabActiva = signal<TabActiva>('saip');
  readonly solicitudExpandida = signal<number | null>(null);

  readonly total = computed(() => this.solicitudes().length);

  readonly enProceso = computed(() =>
    this.solicitudes().filter((solicitud) =>
      solicitud.estado === 'RECEPCIONADA'
      || solicitud.estado === 'EN_REVISION'
      || solicitud.estado === 'PENDIENTE_INFORMACION'
    ).length,
  );

  readonly atendidas = computed(() =>
    this.solicitudes().filter((solicitud) =>
      solicitud.estado === 'RESPONDIDA'
      || solicitud.estado === 'DENEGADA'
      || solicitud.estado === 'CONCLUIDA'
    ).length,
  );

  readonly vencidas = computed(() => this.solicitudes().filter((solicitud) => solicitud.estado === 'VENCIDA').length);

  readonly totalApelaciones = computed(() => this.apelaciones().length);

  readonly puedeApelar = computed(() =>
    this.solicitudes().filter((s) =>
      (s.estado === 'RESPONDIDA' || s.estado === 'DENEGADA' || s.estado === 'VENCIDA')
      && !this.apelaciones().some((a) => a.solicitudId === (s.idSolicitud ?? s.id))
    ).length > 0,
  );

  readonly solicitudesApelables = computed(() =>
    this.solicitudes().filter((s) =>
      (s.estado === 'RESPONDIDA' || s.estado === 'DENEGADA' || s.estado === 'VENCIDA')
      && !this.apelaciones().some((a) => a.solicitudId === (s.idSolicitud ?? s.id))
    ),
  );

  constructor() {
    this.inicializarDesdeSesion();
  }

  cambiarTab(tab: TabActiva): void {
    this.tabActiva.set(tab);
    if (tab === 'apelaciones' && this.apelaciones().length === 0 && this.ciudadanoId()) {
      this.cargarApelaciones();
    }
  }

  toggleDetalle(solicitud: Solicitud): void {
    const sid = solicitud.idSolicitud ?? solicitud.id ?? 0;
    this.solicitudExpandida.set(this.solicitudExpandida() === sid ? null : sid);
  }

  cargarSolicitudes(): void {
    const idCiudadano = this.ciudadanoId();

    if (!idCiudadano) {
      this.loading.set(false);
      this.error.set('No se encontro un ciudadano asociado a la sesion.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.solicitudService.findByCiudadanoId(idCiudadano).subscribe({
      next: (data) => {
        this.solicitudes.set(data);
        this.loading.set(false);
        // Tambien cargar apelaciones del ciudadano
        this.cargarApelaciones();
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar sus solicitudes SAIP.');
      },
    });
  }

  cargarApelaciones(): void {
    const idCiudadano = this.ciudadanoId();
    if (!idCiudadano) {
      return;
    }

    this.loadingApelaciones.set(true);
    this.apelacionService.findByCiudadanoId(idCiudadano).subscribe({
      next: (data) => {
        this.apelaciones.set(data);
        this.loadingApelaciones.set(false);
      },
      error: () => {
        this.loadingApelaciones.set(false);
      },
    });
  }

  getEstadoClass(estado: string): string {
    const classes: Record<string, string> = {
      RECEPCIONADA: 'badge-info',
      EN_REVISION: 'badge-warning',
      PENDIENTE_INFORMACION: 'badge-warning',
      RESPONDIDA: 'badge-success',
      CONCLUIDA: 'badge-success',
      DENEGADA: 'badge-danger',
      VENCIDA: 'badge-dark',
    };

    return classes[estado] ?? 'badge-muted';
  }

  getApelacionEstadoClass(estado: string): string {
    const classes: Record<string, string> = {
      PENDIENTE_ELEVACION: 'badge-warning',
      EN_CALIFICACION_1: 'badge-warning',
      EN_SUBSANACION: 'badge-warning',
      EN_CALIFICACION_2: 'badge-info',
      NOTIFICACION_SEGUNDA_CALIFICACION: 'badge-info',
      EN_RESOLUCION: 'badge-info',
      RESUELTO: 'badge-success',
      RESUELTO_FUNDADO: 'badge-success',
      RESUELTO_FUNDADO_EN_PARTE: 'badge-success',
      RESUELTO_INFUNDADO: 'badge-danger',
      RESUELTO_INFUNDADO_EN_PARTE: 'badge-danger',
      RESUELTO_IMPROCEDENTE: 'badge-danger',
      TENER_POR_NO_PRESENTADO: 'badge-dark',
    };

    return classes[estado] ?? 'badge-muted';
  }

  getEstadoLabel(estado: string): string {
    const labels: Record<string, string> = {
      RECEPCIONADA: 'Recepcionada',
      EN_REVISION: 'En revision',
      PENDIENTE_INFORMACION: 'Pendiente informacion',
      RESPONDIDA: 'Respondida',
      CONCLUIDA: 'Concluida',
      DENEGADA: 'Denegada',
      VENCIDA: 'Vencida',
      PENDIENTE_ELEVACION: 'Pendiente elevacion',
      EN_CALIFICACION_1: '1ra. Calificacion',
      EN_SUBSANACION: 'En subsanacion',
      EN_CALIFICACION_2: '2da. Calificacion',
      NOTIFICACION_SEGUNDA_CALIFICACION: 'Notificacion',
      EN_RESOLUCION: 'En resolucion',
      RESUELTO: 'Resuelto',
      RESUELTO_FUNDADO: 'Fundado',
      RESUELTO_FUNDADO_EN_PARTE: 'Fundado en parte',
      RESUELTO_INFUNDADO: 'Infundado',
      RESUELTO_INFUNDADO_EN_PARTE: 'Infundado en parte',
      RESUELTO_IMPROCEDENTE: 'Improcedente',
      TENER_POR_NO_PRESENTADO: 'No presentado',
      CONCLUSION_SUSTRACCION_MATERIA: 'Conclusion sustraccion',
      CONCLUSION_DESISTIMIENTO: 'Desistimiento',
    };

    return labels[estado] ?? estado;
  }

  puedeApelarSolicitud(solicitud: Solicitud): boolean {
    const estadosApelables = ['RESPONDIDA', 'DENEGADA', 'VENCIDA'];
    if (!estadosApelables.includes(solicitud.estado)) {
      return false;
    }
    return !this.apelaciones().some((a) => a.solicitudId === (solicitud.idSolicitud ?? solicitud.id));
  }

  puedeSubsanar(apelacion: Apelacion): boolean {
    return apelacion.estado === 'EN_SUBSANACION';
  }

  cerrarSesion(): void {
    this.authService.cerrarSesion();
    void this.router.navigate(['/login']);
  }

  private inicializarDesdeSesion(): void {
    if (!isPlatformBrowser(this.platformId)) {
      this.loading.set(false);
      return;
    }

    const rawSesion = localStorage.getItem('usuario');
    if (!rawSesion) {
      this.loading.set(false);
      this.error.set('No hay sesion activa de ciudadano.');
      return;
    }

    try {
      const sesion = JSON.parse(rawSesion) as SesionCiudadano;

      if (typeof sesion.ciudadanoId !== 'number' || sesion.ciudadanoId <= 0) {
        this.loading.set(false);
        this.error.set('La sesion no contiene un ciudadano valido.');
        return;
      }

      this.ciudadanoId.set(sesion.ciudadanoId);
      this.nombreUsuario.set(sesion.nombre ?? 'Ciudadano');
      this.cargarSolicitudes();
    } catch {
      this.loading.set(false);
      this.error.set('No se pudo leer la sesion de usuario.');
    }
  }
}
