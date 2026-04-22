import { ChangeDetectionStrategy, Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { DatePipe, isPlatformBrowser } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Solicitud } from '../../models/solicitud.model';
import { SolicitudService } from '../../services/solicitud.service';
import { AuthService } from '../../services/auth.service';

interface SesionCiudadano {
  ciudadanoId?: number;
  nombre?: string;
}

@Component({
  selector: 'app-ciudadano-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink],
  templateUrl: './ciudadano-dashboard.component.html',
  styleUrl: './ciudadano-dashboard.component.css',
})
export class CiudadanoDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly solicitudService = inject(SolicitudService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly ciudadanoId = signal<number | null>(null);
  readonly nombreUsuario = signal('Ciudadano');
  readonly solicitudes = signal<Solicitud[]>([]);

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

  constructor() {
    this.inicializarDesdeSesion();
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
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudieron cargar sus solicitudes SAIP.');
      },
    });
  }

  getEstadoClass(estado: string): string {
    const classes: Record<string, string> = {
      RECEPCIONADA: 'bg-blue-100 text-blue-700 border border-blue-200',
      EN_REVISION: 'bg-amber-100 text-amber-700 border border-amber-200',
      PENDIENTE_INFORMACION: 'bg-amber-100 text-amber-700 border border-amber-200',
      RESPONDIDA: 'bg-emerald-100 text-emerald-700 border border-emerald-200',
      CONCLUIDA: 'bg-emerald-100 text-emerald-700 border border-emerald-200',
      DENEGADA: 'bg-red-100 text-red-700 border border-red-200',
      VENCIDA: 'bg-neutral-800 text-white border border-neutral-700',
    };

    return classes[estado] ?? 'bg-neutral-100 text-neutral-700 border border-neutral-200';
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
