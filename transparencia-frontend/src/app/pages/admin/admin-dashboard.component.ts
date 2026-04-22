import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { SolicitudService } from '../../services/solicitud.service';
import { TtaipService } from '../../services/ttaip.service';

interface SolicitudEstadisticas {
  total?: number;
  recepcionadas?: number;
  enRevision?: number;
  pendienteInformacion?: number;
  respondidas?: number;
  denegadas?: number;
  concluidas?: number;
  vencidas?: number;
}

interface TtaipEstadisticas {
  total?: number;
  pendientesAdmision?: number;
  pendientes?: number;
  enProceso?: number;
  enSubsanacion?: number;
  resueltas?: number;
}

@Component({
  selector: 'app-admin-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly solicitudService = inject(SolicitudService);
  private readonly ttaipService = inject(TtaipService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly totalSolicitudes = signal(0);
  readonly recepcionadas = signal(0);
  readonly enRevision = signal(0);
  readonly respondidas = signal(0);
  readonly denegadas = signal(0);
  readonly vencidas = signal(0);

  readonly totalApelaciones = signal(0);
  readonly pendientesTtaip = signal(0);
  readonly enProcesoTtaip = signal(0);
  readonly enSubsanacionTtaip = signal(0);
  readonly resueltasTtaip = signal(0);

  readonly tasaAtencion = computed(() => {
    const total = this.totalSolicitudes();
    if (total === 0) {
      return 0;
    }

    const atendidas = this.respondidas() + this.denegadas();
    return Math.round((atendidas / total) * 100);
  });

  constructor() {
    this.cargarResumen();
  }

  cargarResumen(): void {
    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      solicitudes: this.solicitudService.getEstadisticas(),
      apelaciones: this.ttaipService.getEstadisticas(),
    }).subscribe({
      next: ({ solicitudes, apelaciones }) => {
        this.aplicarEstadisticasSolicitudes(solicitudes as SolicitudEstadisticas);
        this.aplicarEstadisticasTtaip(apelaciones as TtaipEstadisticas);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudo cargar el resumen administrativo.');
      },
    });
  }

  cerrarSesion(): void {
    this.authService.cerrarSesion();
    void this.router.navigate(['/acceso-admin']);
  }

  private aplicarEstadisticasSolicitudes(data: SolicitudEstadisticas): void {
    this.totalSolicitudes.set(data.total ?? 0);
    this.recepcionadas.set(data.recepcionadas ?? 0);
    this.enRevision.set((data.enRevision ?? 0) + (data.pendienteInformacion ?? 0));
    this.respondidas.set((data.respondidas ?? 0) + (data.concluidas ?? 0));
    this.denegadas.set(data.denegadas ?? 0);
    this.vencidas.set(data.vencidas ?? 0);
  }

  private aplicarEstadisticasTtaip(data: TtaipEstadisticas): void {
    this.totalApelaciones.set(data.total ?? 0);
    this.pendientesTtaip.set(data.pendientesAdmision ?? data.pendientes ?? 0);
    this.enProcesoTtaip.set(data.enProceso ?? 0);
    this.enSubsanacionTtaip.set(data.enSubsanacion ?? 0);
    this.resueltasTtaip.set(data.resueltas ?? 0);
  }
}
