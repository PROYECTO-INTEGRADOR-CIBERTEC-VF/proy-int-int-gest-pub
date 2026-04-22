import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Apelacion } from '../../models/apelacion.model';
import { TtaipService } from '../../services/ttaip.service';

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

  apelacion = signal<Apelacion | null>(null);
  loading = signal<boolean>(false);
  loadingApelacion = signal<boolean>(true);
  error = signal<string>('');
  mensaje = signal<string>('');
  activeTab = signal<string>('subsanacion');

  decision = '';
  fundamentos = '';

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

  getObservaciones(): string[] {
    const resolucionPrimera = this.apelacion()?.resolucionPrimeraCalificacion;
    const observaciones = resolucionPrimera?.observaciones ?? '';

    if (!observaciones.trim()) {
      return [];
    }

    return observaciones
      .split(';')
      .map((observacion) => observacion.trim())
      .filter((observacion) => observacion.length > 0);
  }

  getSubsanacionFromFundamentos(): string {
    const fundamentos = this.apelacion()?.fundamentos ?? '';

    if (!fundamentos.includes('--- SUBSANACION ---')) {
      return '';
    }

    const parts = fundamentos.split('--- SUBSANACION ---');
    return parts.length > 1 ? parts[1].trim() : '';
  }

  getOriginalFundamentos(): string {
    const fundamentos = this.apelacion()?.fundamentos ?? '';

    if (!fundamentos.includes('--- SUBSANACION ---')) {
      return fundamentos;
    }

    return fundamentos.split('--- SUBSANACION ---')[0].trim();
  }

  enviarCalificacion(): void {
    const apelacion = this.apelacion();
    if (!apelacion) {
      return;
    }

    const apelacionId = apelacion.idApelacion ?? apelacion.id;
    if (!apelacionId) {
      this.error.set('No se pudo identificar la apelacion para registrar la decision.');
      return;
    }

    if (!this.decision) {
      this.error.set('Debe seleccionar una decision');
      return;
    }

    if (!this.fundamentos.trim()) {
      this.error.set('Los fundamentos son obligatorios');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    const request = { fundamentos: this.fundamentos.trim() };

    if (this.decision === 'admitir') {
      this.ttaipService.admitirApelacion(apelacionId, request).subscribe({
        next: () => this.exito('Apelacion admitida a tramite'),
        error: (err) => this.errorHandler(err),
      });
      return;
    }

    this.ttaipService.inadmitirApelacion(apelacionId, request).subscribe({
      next: () => this.exito('Apelacion declarada improcedente'),
      error: (err) => this.errorHandler(err),
    });
  }

  private exito(msg: string): void {
    this.loading.set(false);
    this.mensaje.set(msg);
    setTimeout(() => this.router.navigate(['/ttaip']), 2000);
  }

  private errorHandler(err: any): void {
    this.loading.set(false);
    this.error.set(err?.error?.mensaje || 'Error al procesar la segunda calificacion');
  }
}
