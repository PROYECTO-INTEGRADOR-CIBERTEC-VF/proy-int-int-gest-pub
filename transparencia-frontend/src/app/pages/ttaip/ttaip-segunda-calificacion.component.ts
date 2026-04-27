import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Apelacion } from '../../models/apelacion.model';
import { TtaipService } from '../../services/ttaip.service';
import { TtaipSegundaCalificacionService, SegundaCalificacionRequest } from './ttaip-segunda-calificacion.service';

@Component({
  selector: 'app-ttaip-segunda-calificacion',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ttaip-segunda-calificacion.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TtaipSegundaCalificacionComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly ttaipService = inject(TtaipService);
  private readonly segundaCalificacionService = inject(TtaipSegundaCalificacionService);

  apelacion = signal<Apelacion | null>(null);
  loading = signal<boolean>(false);
  loadingApelacion = signal<boolean>(true);
  error = signal<string>('');
  mensaje = signal<string>('');
  activeTab = signal<string>('subsanacion');

  decision = '';
  fundamentos = '';
  archivoAdjunto: File | null = null; // Guardará el PDF

  ngOnInit(): void {
    const expediente = this.route.snapshot.paramMap.get('expediente');
    if (expediente) {
      this.cargarApelacion(expediente);
      return;
    }

    this.loadingApelacion.set(false);
    this.error.set('No se encontro el expediente a evaluar.');
  }

  cargarApelacion(expediente: string): void {
    this.loadingApelacion.set(true);
    this.error.set('');

    this.ttaipService.getApelacionPorExpediente(expediente).subscribe({
      next: (data) => {
        if (!data) {
          this.error.set('No se encontro la apelacion para el expediente indicado.');
          this.loadingApelacion.set(false);
          return;
        }
        this.apelacion.set(data);
        this.loadingApelacion.set(false);
      },
      error: () => {
        this.error.set('Error al cargar la apelacion');
        this.loadingApelacion.set(false);
      },
    });
  }

  cambiarTab(tab: string): void {
    this.activeTab.set(tab);
  }

  // --- Atrapa el archivo desde el HTML ---
  onArchivoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.archivoAdjunto = input.files[0];
    }
  }

  getObservaciones(): string[] {
    const resolucionPrimera = this.apelacion()?.resolucionPrimeraCalificacion;
    const observaciones = resolucionPrimera?.observaciones ?? '';
    if (!observaciones.trim()) return [];
    return observaciones.split(';').map(o => o.trim()).filter(o => o.length > 0);
  }

  getSubsanacionFromFundamentos(): string {
    const fundamentos = this.apelacion()?.fundamentos ?? '';
    if (!fundamentos.includes('--- SUBSANACION ---')) return '';
    const parts = fundamentos.split('--- SUBSANACION ---');
    return parts.length > 1 ? parts[1].trim() : '';
  }

  getOriginalFundamentos(): string {
    const fundamentos = this.apelacion()?.fundamentos ?? '';
    if (!fundamentos.includes('--- SUBSANACION ---')) return fundamentos;
    return fundamentos.split('--- SUBSANACION ---')[0].trim();
  }

  enviarCalificacion(): void {
    const apelacion = this.apelacion();
    if (!apelacion) return;

    const apelacionId = apelacion.idApelacion ?? apelacion.id;
    if (!apelacionId) {
      this.error.set('No se pudo identificar la apelacion.');
      return;
    }

    if (!this.decision) {
      this.error.set('Debe seleccionar una decisión (ADMISIBLE, IMPROCEDENTE o TENER_POR_NO_PRESENTADO).');
      return;
    }

    if (!this.fundamentos.trim()) {
      this.error.set('Los fundamentos son obligatorios.');
      return;
    }

    if (!this.archivoAdjunto) {
      this.error.set('Debe adjuntar el documento de resolución firmado (PDF).');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    const request: SegundaCalificacionRequest = {
      decision: this.decision.toUpperCase(),
      fundamentos: this.fundamentos.trim()
    };

    this.segundaCalificacionService.notificarSegundaCalificacion(apelacionId, request, this.archivoAdjunto).subscribe({
      next: () => this.exito(`Decisión registrada correctamente.`),
      error: (err) => this.errorHandler(err),
    });
  }

  private exito(msg: string): void {
    this.loading.set(false);
    this.mensaje.set(msg);
    setTimeout(() => this.router.navigate(['/ttaip']), 2500);
  }

  private errorHandler(err: any): void {
    this.loading.set(false);
    this.error.set(err?.error?.error || 'Error al procesar en el servidor.');
  }
}
