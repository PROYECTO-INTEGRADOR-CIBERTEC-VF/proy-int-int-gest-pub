import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Solicitud } from '../../models/solicitud.model';
import { SolicitudService } from '../../services/solicitud.service';

type TabDetalle = 'informacion' | 'documentos' | 'historial';
type HistorialTono = 'info' | 'warning' | 'success' | 'muted';

interface DocumentoVista {
  nombre: string;
  tipo: string;
  tamano: string;
}

interface HistorialEvento {
  titulo: string;
  descripcion: string;
  fecha?: string;
  tono: HistorialTono;
}

@Component({
  selector: 'app-funcionario-solicitud-detalle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe],
  templateUrl: './funcionario-solicitud-detalle.component.html',
  styleUrl: './funcionario-solicitud-detalle.component.css',
})
export class FuncionarioSolicitudDetalleComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly solicitudService = inject(SolicitudService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly solicitud = signal<Solicitud | null>(null);
  readonly activeTab = signal<TabDetalle>('informacion');

  readonly documentos = computed<DocumentoVista[]>(() => {
    const solicitud = this.solicitud();
    if (!solicitud) {
      return [];
    }

    const documentos: DocumentoVista[] = [];

    if (solicitud.formatoDigital) {
      documentos.push({
        nombre: 'formato-digital-solicitado.txt',
        tipo: 'TXT',
        tamano: '1 KB',
      });
    }

    if (solicitud.formatoFisico) {
      documentos.push({
        nombre: 'formato-fisico-solicitado.txt',
        tipo: 'TXT',
        tamano: '1 KB',
      });
    }

    if (solicitud.tipoInformacion) {
      documentos.push({
        nombre: 'tipo-informacion-solicitada.txt',
        tipo: 'TXT',
        tamano: '1 KB',
      });
    }

    return documentos;
  });

  readonly historial = computed<HistorialEvento[]>(() => {
    const solicitud = this.solicitud();
    if (!solicitud) {
      return [];
    }

    const eventos: HistorialEvento[] = [
      {
        titulo: 'Solicitud recepcionada',
        descripcion: 'Se registro la solicitud SAIP y fue asignada a la entidad.',
        fecha: solicitud.fechaPresentacion,
        tono: 'info',
      },
    ];

    if (solicitud.estado === 'EN_REVISION' || solicitud.estado === 'PENDIENTE_INFORMACION') {
      eventos.push({
        titulo: 'Solicitud en revision',
        descripcion: 'El funcionario esta evaluando la informacion solicitada.',
        tono: 'warning',
      });
    }

    if (this.tieneRespuesta(solicitud)) {
      eventos.push({
        titulo: 'Respuesta registrada',
        descripcion: 'La entidad emitio respuesta a la solicitud.',
        tono: 'success',
      });
    }

    if (this.esSilencioAdministrativo(solicitud)) {
      eventos.push({
        titulo: 'Silencio administrativo',
        descripcion: 'La solicitud excedio el plazo legal de respuesta.',
        fecha: solicitud.fechaLimite,
        tono: 'muted',
      });
    }

    return eventos;
  });

  constructor() {
    this.cargarSolicitudDesdeRuta();
  }

  seleccionarTab(tab: TabDetalle): void {
    this.activeTab.set(tab);
  }

  puedeResponder(solicitud: Solicitud): boolean {
    return !this.esSilencioAdministrativo(solicitud) && !this.tieneRespuesta(solicitud);
  }

  obtenerSemaforoEtiqueta(solicitud: Solicitud): string {
    if (this.esSilencioAdministrativo(solicitud)) {
      return 'NEGRO';
    }

    const dias = this.obtenerDiasRestantes(solicitud);
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

  obtenerEstadoClase(estado: string): string {
    if (estado === 'RESPONDIDA' || estado === 'CONCLUIDA') {
      return 'bg-emerald-100 text-emerald-700 border border-emerald-200';
    }

    if (estado === 'DENEGADA') {
      return 'bg-red-100 text-red-700 border border-red-200';
    }

    if (estado === 'VENCIDA') {
      return 'bg-neutral-800 text-white border border-neutral-700';
    }

    if (estado === 'EN_REVISION') {
      return 'bg-blue-100 text-blue-700 border border-blue-200';
    }

    return 'bg-amber-100 text-amber-700 border border-amber-200';
  }

  obtenerTonoPuntoClase(tono: HistorialTono): string {
    if (tono === 'success') {
      return 'bg-emerald-500';
    }

    if (tono === 'warning') {
      return 'bg-amber-500';
    }

    if (tono === 'muted') {
      return 'bg-neutral-500';
    }

    return 'bg-blue-500';
  }

  obtenerTonoAnilloClase(tono: HistorialTono): string {
    if (tono === 'success') {
      return 'ring-emerald-100';
    }

    if (tono === 'warning') {
      return 'ring-amber-100';
    }

    if (tono === 'muted') {
      return 'ring-neutral-200';
    }

    return 'ring-blue-100';
  }

  obtenerDiasRestantes(solicitud: Solicitud): number | null {
    return typeof solicitud.diasRestantes === 'number' ? solicitud.diasRestantes : null;
  }

  obtenerTextoDiasRestantes(solicitud: Solicitud): string {
    const dias = this.obtenerDiasRestantes(solicitud);
    if (dias === null) {
      return 'No disponible';
    }

    if (dias < 0) {
      return `${Math.abs(dias)} dias de retraso`;
    }

    return `${dias} dias`;
  }

  private cargarSolicitudDesdeRuta(): void {
    const expediente = this.route.snapshot.paramMap.get('expediente');
    if (!expediente) {
      this.loading.set(false);
      this.error.set('No se recibio expediente para mostrar el detalle.');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.solicitudService.findByExpediente(expediente).subscribe({
      next: (solicitud) => {
        this.solicitud.set(solicitud);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No se pudo cargar la solicitud seleccionada.');
      },
    });
  }

  private tieneRespuesta(solicitud: Solicitud): boolean {
    return (
      solicitud.estado === 'RESPONDIDA' ||
      solicitud.estado === 'DENEGADA' ||
      solicitud.estado === 'CONCLUIDA'
    );
  }

  private esSilencioAdministrativo(solicitud: Solicitud): boolean {
    const dias = this.obtenerDiasRestantes(solicitud);
    const porEstado = solicitud.estado === 'VENCIDA';
    const porDias = dias !== null && dias < 0;
    const porSemaforo = solicitud.semaforo === 'NEGRO';

    return porEstado || porDias || porSemaforo;
  }
}
