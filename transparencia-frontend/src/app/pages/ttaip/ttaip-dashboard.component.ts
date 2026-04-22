import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Apelacion } from '../../models/apelacion.model';
import { TtaipService } from '../../services/ttaip.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-ttaip-dashboard',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './ttaip-dashboard.component.html',
  styleUrl: './ttaip-dashboard.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TtaipDashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ttaipService = inject(TtaipService);

  apelaciones = signal<Apelacion[]>([]);
  loading = signal<boolean>(true);
  error = signal<string>('');

  pendientesAdmision = signal<number>(0);
  enSegundaCalificacion = signal<number>(0);
  enProceso = signal<number>(0);
  enSubsanacion = signal<number>(0);
  resueltos = signal<number>(0);

  filtroActual = signal<string>('pendientes');

  ngOnInit(): void {
    this.cargarEstadisticas();
    this.cargarPendientes();
  }

  cargarEstadisticas(): void {
    this.ttaipService.getEstadisticas().subscribe({
      next: (data) => {
        this.pendientesAdmision.set(data.pendientesAdmision ?? data.pendientes ?? 0);
        this.enProceso.set(data.enProceso ?? 0);
        this.enSubsanacion.set(data.enSubsanacion ?? 0);
        this.resueltos.set(data.resueltas ?? 0);
      },
      error: () => {
        this.pendientesAdmision.set(0);
        this.enProceso.set(0);
        this.enSubsanacion.set(0);
        this.resueltos.set(0);
      },
    });

    this.ttaipService.getSegundaCalificacion().subscribe({
      next: (data) => this.enSegundaCalificacion.set(data.length),
      error: () => this.enSegundaCalificacion.set(0),
    });
  }

  cargarPendientes(): void {
    this.cargarLista('pendientes', () => this.ttaipService.getPendientes());
  }

  cargarEnAnalisis(): void {
    this.cargarLista('analisis', () => this.ttaipService.getEnAnalisis());
  }

  cargarSubsanacion(): void {
    this.cargarLista('subsanacion', () => this.ttaipService.getSubsanacion());
  }

  cargarSegundaCalificacion(): void {
    this.cargarLista('segunda', () => this.ttaipService.getSegundaCalificacion());
  }

  cargarResueltas(): void {
    this.cargarLista('resueltas', () => this.ttaipService.getResueltas());
  }

  private cargarLista(filtro: string, loader: () => ReturnType<TtaipService['getPendientes']>): void {
    this.loading.set(true);
    this.error.set('');
    this.filtroActual.set(filtro);

    loader().subscribe({
      next: (data) => {
        this.apelaciones.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.mensaje ?? 'Error al cargar apelaciones');
      },
    });
  }

  getEstadoBadgeClass(estado: string): string {
    const clases: Record<string, string> = {
      PENDIENTE_ELEVACION: 'badge-warning',
      EN_CALIFICACION_1: 'badge-warning',
      EN_CALIFICACION_2: 'badge-warning',
      EN_SUBSANACION: 'badge-warning',
      NOTIFICACION_SEGUNDA_CALIFICACION: 'badge-in-process',
      EN_RESOLUCION: 'badge-in-process',
      RESUELTO: 'badge-success',
      RESUELTO_FUNDADO: 'badge-success',
      RESUELTO_FUNDADO_EN_PARTE: 'badge-success',
      RESUELTO_INFUNDADO: 'badge-muted',
      RESUELTO_INFUNDADO_EN_PARTE: 'badge-muted',
      RESUELTO_IMPROCEDENTE: 'badge-danger',
      TENER_POR_NO_PRESENTADO: 'badge-danger',
      CONCLUSION_SUSTRACCION_MATERIA: 'badge-success',
      CONCLUSION_DESISTIMIENTO: 'badge-muted',
    };

    return clases[estado] || 'badge-muted';
  }

  getSolicitudReferencia(apelacion: Apelacion): string {
    return apelacion.solicitudExpediente || 'Sin expediente SAIP';
  }

  cerrarSesion(): void {
    this.authService.cerrarSesion();
    void this.router.navigate(['/acceso-ttaip']);
  }
}
