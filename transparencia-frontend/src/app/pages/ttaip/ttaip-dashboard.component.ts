import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

type TabId = 'pendientes' | 'analisis' | 'subsanacion' | 'segunda' | 'resueltas';

interface TabConfig {
  id: TabId;
  label: string;
}

interface ApelacionBandeja {
  expediente: string;
  ciudadano: string;
  entidad: string;
  asunto: string;
  fecha: string;
  estado: string;
}

@Component({
  selector: 'app-ttaip-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ttaip-dashboard.component.html',
  styleUrl: './ttaip-dashboard.component.css'
})
export class TtaipDashboardComponent implements OnInit {
  readonly tabs: ReadonlyArray<TabConfig> = [
    { id: 'pendientes', label: 'Pendientes' },
    { id: 'analisis', label: 'En análisis' },
    { id: 'subsanacion', label: 'Subsanación' },
    { id: 'segunda', label: 'Segunda calificación' },
    { id: 'resueltas', label: 'Resueltas' }
  ];

  readonly tabActivo = signal<TabId>('pendientes');
  readonly cargando = signal<boolean>(false);
  readonly apelaciones = signal<ApelacionBandeja[]>([]);
  readonly conteos = signal<Record<TabId, number>>({
    pendientes: 0,
    analisis: 0,
    subsanacion: 0,
    segunda: 0,
    resueltas: 0
  });

  private readonly cache = new Map<TabId, ApelacionBandeja[]>();

  private readonly datosPorTab: Record<TabId, ApelacionBandeja[]> = {
    pendientes: [
      {
        expediente: '00237-2026-JUS/TTAIP',
        ciudadano: 'Carlos Ruiz Rojas',
        entidad: 'MINEDU',
        asunto: 'Solicitud de información de contratos de infraestructura escolar',
        fecha: '2026-04-14',
        estado: 'PENDIENTE_ELEVACION'
      },
      {
        expediente: '00241-2026-JUS/TTAIP',
        ciudadano: 'Ana Torres Díaz',
        entidad: 'MINSA',
        asunto: 'Entrega de reportes de abastecimiento de medicamentos',
        fecha: '2026-04-15',
        estado: 'EN_CALIFICACION_1'
      }
    ],
    analisis: [
      {
        expediente: '00225-2026-JUS/TTAIP',
        ciudadano: 'Luis Mendoza Vega',
        entidad: 'PRODUCE',
        asunto: 'Acceso a expedientes de fiscalización del año 2025',
        fecha: '2026-04-10',
        estado: 'EN_CALIFICACION_1'
      },
      {
        expediente: '00227-2026-JUS/TTAIP',
        ciudadano: 'María Salazar Quispe',
        entidad: 'MTC',
        asunto: 'Transparencia sobre cronograma de ejecución vial',
        fecha: '2026-04-11',
        estado: 'EN_CALIFICACION_1'
      }
    ],
    subsanacion: [
      {
        expediente: '00216-2026-JUS/TTAIP',
        ciudadano: 'Pedro Fernández Ramos',
        entidad: 'SUNAT',
        asunto: 'Solicitud de criterios internos de clasificación documental',
        fecha: '2026-04-08',
        estado: 'EN_SUBSANACION'
      }
    ],
    segunda: [
      {
        expediente: '00209-2026-JUS/TTAIP',
        ciudadano: 'Elena Paredes Soto',
        entidad: 'MEF',
        asunto: 'Revisión de respuesta parcial sobre presupuesto público',
        fecha: '2026-04-07',
        estado: 'EN_CALIFICACION_2'
      },
      {
        expediente: '00212-2026-JUS/TTAIP',
        ciudadano: 'Jorge Huamán Ríos',
        entidad: 'RENIEC',
        asunto: 'Rectificación de denegatoria por reserva de información',
        fecha: '2026-04-08',
        estado: 'EN_CALIFICACION_2'
      }
    ],
    resueltas: [
      {
        expediente: '00198-2026-JUS/TTAIP',
        ciudadano: 'Rosario Gutiérrez León',
        entidad: 'MINEM',
        asunto: 'Acceso a estudios técnicos de impacto ambiental',
        fecha: '2026-04-03',
        estado: 'RESUELTO_FUNDADO'
      },
      {
        expediente: '00201-2026-JUS/TTAIP',
        ciudadano: 'Manuel Rojas Castro',
        entidad: 'OSINERGMIN',
        asunto: 'Entrega de informes de supervisión anual',
        fecha: '2026-04-04',
        estado: 'RESUELTO_IMPROCEDENTE'
      }
    ]
  };

  ngOnInit(): void {
    this.conteos.set({
      pendientes: this.datosPorTab.pendientes.length,
      analisis: this.datosPorTab.analisis.length,
      subsanacion: this.datosPorTab.subsanacion.length,
      segunda: this.datosPorTab.segunda.length,
      resueltas: this.datosPorTab.resueltas.length
    });

    this.seleccionarTab('pendientes');
  }

  seleccionarTab(tab: TabId): void {
    this.tabActivo.set(tab);

    // Carga lazy por pestaña: cada lista se calcula una sola vez y queda en cache.
    if (!this.cache.has(tab)) {
      this.cargando.set(true);
      this.cache.set(tab, this.datosPorTab[tab]);
      this.cargando.set(false);
    }

    this.apelaciones.set(this.cache.get(tab) ?? []);
  }

  obtenerConteo(tab: TabId): number {
    return this.conteos()[tab];
  }

  getEstadoClass(estado: string): string {
    const clases: Record<string, string> = {
      PENDIENTE_ELEVACION: 'bg-amber-100 text-amber-800',
      EN_CALIFICACION_1: 'bg-indigo-100 text-indigo-800',
      EN_SUBSANACION: 'bg-rose-100 text-rose-800',
      EN_CALIFICACION_2: 'bg-orange-100 text-orange-800',
      EN_RESOLUCION: 'bg-violet-100 text-violet-800',
      RESUELTO_FUNDADO: 'bg-emerald-100 text-emerald-800',
      RESUELTO_FUNDADO_EN_PARTE: 'bg-emerald-100 text-emerald-800',
      RESUELTO_INFUNDADO: 'bg-slate-100 text-slate-800',
      RESUELTO_INFUNDADO_EN_PARTE: 'bg-slate-100 text-slate-800',
      RESUELTO_IMPROCEDENTE: 'bg-red-100 text-red-800'
    };

    return clases[estado] ?? 'bg-slate-100 text-slate-700';
  }

  textoAccion(estado: string): string {
    if (estado === 'PENDIENTE_ELEVACION' || estado === 'EN_CALIFICACION_1') {
      return 'Calificar';
    }
    if (estado === 'EN_CALIFICACION_2') {
      return '2da calificación';
    }
    if (estado === 'EN_RESOLUCION') {
      return 'Resolver';
    }
    return 'En revisión';
  }

  rutaAccion(apelacion: ApelacionBandeja): string[] | null {
    if (apelacion.estado === 'PENDIENTE_ELEVACION' || apelacion.estado === 'EN_CALIFICACION_1') {
      return ['/ttaip/calificar', apelacion.expediente];
    }
    if (apelacion.estado === 'EN_CALIFICACION_2') {
      return ['/ttaip/segunda-calificacion', apelacion.expediente];
    }
    if (apelacion.estado === 'EN_RESOLUCION') {
      return ['/ttaip/resolver', apelacion.expediente];
    }
    return null;
  }
}
