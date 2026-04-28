import { ChangeDetectionStrategy, Component, OnInit, inject, signal, computed } from '@angular/core';
import { NgClass } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Apelacion } from '../../models/apelacion.model';
import { TtaipService } from '../../services/ttaip.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-ttaip-dashboard',
  imports: [RouterLink, FormsModule, NgClass],
  templateUrl: './ttaip-dashboard.component.html',
  styleUrl: './ttaip-dashboard.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TtaipDashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ttaipService = inject(TtaipService);

  // Signals para los datos
  apelaciones = signal<Apelacion[]>([]);
  loading = signal<boolean>(true);
  error = signal<string>('');

  // Signals para las métricas
  pendientesAdmision = signal<number>(0);
  enSegundaCalificacion = signal<number>(0);
  enProceso = signal<number>(0);
  enSubsanacion = signal<number>(0);
  resueltos = signal<number>(0);

  filtroActual = signal<string>('todas');
  terminoBusqueda = signal<string>('');

  // Lista filtrada por el buscador
  apelacionesFiltradas = computed(() => {
    const term = this.terminoBusqueda().toLowerCase().trim();
    if (!term) return this.apelaciones();

    return this.apelaciones().filter(
      (a) =>
        (a.expediente && a.expediente.toLowerCase().includes(term)) ||
        (a.ciudadanoNombre && a.ciudadanoNombre.toLowerCase().includes(term)),
    );
  });

  // Alerta de plazos
  tieneApelacionesUrgentes = computed(() => {
    return this.apelaciones().some((a) => this.calcularDiasHabiles(a.fechaApelacion) >= 5);
  });

  ngOnInit(): void {
    this.cargarEstadisticas();
    this.cargarTodas();
  }

  cargarEstadisticas(): void {
    this.ttaipService.getEstadisticas().subscribe({
      next: (data) => {
        // Leemos la verdad absoluta directamente del Backend actualizado
        this.pendientesAdmision.set(data.pendientes ?? 0);
        this.enProceso.set(data.enProceso ?? 0);
        this.enSubsanacion.set(data.enSubsanacion ?? 0);
        this.enSegundaCalificacion.set(data.segundaCalificacion ?? 0);
        this.resueltos.set(data.resueltas ?? 0);
      },
      error: () => {
        this.pendientesAdmision.set(0);
        this.enProceso.set(0);
        this.enSubsanacion.set(0);
        this.enSegundaCalificacion.set(0);
        this.resueltos.set(0);
      },
    });
  }

  cargarPendientes(): void {
    this.cargarLista('pendientes', () => this.ttaipService.getPendientes());
  }

  cargarEnAnalisis(): void {
    this.filtroActual.set('analisis');
    this.cargarLista('analisis', () => this.ttaipService.getEnProceso());
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

  private cargarLista(
    filtro: string,
    loader: () => ReturnType<TtaipService['getPendientes']>,
  ): void {
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

  cargarTodas(): void {
    this.cargarLista('todas', () => this.ttaipService.getTodas());
  }

  getEstadoBadgeClass(estado: string): string {
    const clases: Record<string, string> = {
      PENDIENTE_ELEVACION: 'text-yellow-600 bg-yellow-50',
      EN_CALIFICACION_1: 'text-purple-600 bg-purple-50',
      EN_CALIFICACION_2: 'text-blue-600 bg-blue-50',
      EN_SUBSANACION: 'text-red-500 bg-red-50',
      NOTIFICACION_SEGUNDA_CALIFICACION: 'text-blue-600 bg-blue-50',
      EN_RESOLUCION: 'text-purple-600 bg-purple-50',
      RESUELTO: 'text-emerald-600 bg-emerald-50 border border-emerald-200',
      RESUELTO_FUNDADO: 'text-emerald-600 bg-emerald-50 border border-emerald-200',
      RESUELTO_FUNDADO_EN_PARTE: 'text-emerald-600 bg-emerald-50 border border-emerald-200',
      RESUELTO_INFUNDADO: 'text-emerald-600 bg-emerald-50 border border-emerald-200',
      RESUELTO_IMPROCEDENTE: 'text-red-600 bg-red-50 border border-red-200',
      TENER_POR_NO_PRESENTADO: 'text-red-600 bg-red-50 border border-red-200',
      CONCLUSION_SUSTRACCION_MATERIA: 'text-gray-600 bg-gray-50',
      CONCLUSION_DESISTIMIENTO: 'text-gray-600 bg-gray-50',
    };
    return clases[estado] || 'text-gray-600 bg-gray-50';
  }

  getEtapaProceso(estado: string): { titulo: string; desc: string } {
    const etapas: Record<string, { titulo: string; desc: string }> = {
      PENDIENTE_ELEVACION: { titulo: 'Pendiente de Elevación', desc: 'La entidad debe elevar el recurso al TTAIP' },
      EN_CALIFICACION_1: { titulo: '1ra Calificación - Pendiente', desc: 'Apelación recibida, pendiente de calificación inicial' },
      EN_SUBSANACION: { titulo: 'Subsanación - Esperando Documentos', desc: 'Se notificó al administrado para subsanar observaciones' },
      EN_CALIFICACION_2: { titulo: '2da Calificación - Después de Subsanación', desc: 'Revisando documentos subsanados para admisión o rechazo' },
      EN_RESOLUCION: { titulo: 'Resolución Final - En Análisis', desc: 'Analizando el fondo de la apelación admisible' },
      RESUELTO_FUNDADO: { titulo: 'Resuelto - Fundado', desc: 'La resolución final ha sido emitida a favor del administrado' },
      RESUELTO_INFUNDADO: { titulo: 'Resuelto - Infundado', desc: 'La resolución final ha sido emitida rechazando la apelación' },
      RESUELTO_IMPROCEDENTE: { titulo: 'Resuelto - Improcedente', desc: 'La apelación ha sido rechazada definitivamente' },
    };
    if (estado === 'NOTIFICACION_SEGUNDA_CALIFICACION') {
      return {
        titulo: 'Notificación de Admisión',
        desc: 'Preparando notificación de admisión para el ciudadano'
      };
    }
    return etapas[estado] || { titulo: estado, desc: 'Procesando expediente...' };
  }

  calcularDiasHabiles(fechaStr: string | Date | undefined): number {
    if (!fechaStr) return 0;
    const fecha = new Date(fechaStr);
    const hoy = new Date();
    const diffTime = Math.abs(hoy.getTime() - fecha.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  getSolicitudReferencia(apelacion: Apelacion): string {
    return apelacion.solicitudExpediente || 'Sin expediente SAIP';
  }

  // Traductor para las etiquetas (Badges) de la tabla
  getEstadoLabel(estado: string): string {
    const etiquetas: Record<string, string> = {
      'PENDIENTE_ELEVACION': 'En Elevación',
      'EN_CALIFICACION_1': 'En 1ra Calificación',
      'EN_SUBSANACION': 'En Subsanación',
      'EN_CALIFICACION_2': 'En 2da Calificación',
      'NOTIFICACION_SEGUNDA_CALIFICACION': 'En Notificación',
      'EN_RESOLUCION': 'En Resolución',
      'RESUELTO_FUNDADO': 'Resuelto',
      'RESUELTO_INFUNDADO': 'Resuelto',
      'RESUELTO_IMPROCEDENTE': 'Resuelto',
      'TENER_POR_NO_PRESENTADO': 'No Presentado',
      'CONCLUSION_SUSTRACCION_MATERIA': 'Concluido',
      'CONCLUSION_DESISTIMIENTO': 'Desistimiento'
    };

    // Si el estado empieza con RESUELTO, pero no está en la lista, va "Resuelto"
    if (estado.startsWith('RESUELTO')) return 'Resuelto';



    return etiquetas[estado] || estado;
  }

  cerrarSesion(): void {
    this.authService.cerrarSesion();
    void this.router.navigate(['/acceso-ttaip']);
  }
}
