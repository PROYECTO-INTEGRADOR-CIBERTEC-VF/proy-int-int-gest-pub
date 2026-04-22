import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Apelacion } from '../../models/apelacion.model';
import { TtaipService } from '../../services/ttaip.service';

@Component({
  selector: 'app-ttaip-resolver',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ttaip-resolver.component.html',
  styleUrl: './ttaip-resolver.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TtaipResolverComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly ttaipService = inject(TtaipService);

  apelacion = signal<Apelacion | null>(null);
  loading = signal<boolean>(false);
  loadingApelacion = signal<boolean>(true);
  error = signal<string>('');
  mensaje = signal<string>('');

  decision = '';
  fundamentos = '';
  iniciarProcesoDisciplinario = false;

  ngOnInit(): void {
    const expediente = this.route.snapshot.paramMap.get('expediente');
    if (expediente) {
      this.cargarApelacion(expediente);
      return;
    }

    this.loadingApelacion.set(false);
    this.error.set('No se encontro el expediente para resolver.');
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

  enviarResolucion(): void {
    const ap = this.apelacion();
    if (!ap) {
      return;
    }

    const apelacionId = ap.idApelacion ?? ap.id;
    if (!apelacionId) {
      this.error.set('No se pudo identificar la apelacion para emitir la resolucion.');
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

    const request = {
      fundamentos: this.fundamentos.trim(),
      iniciarProcesoDisciplinario: this.iniciarProcesoDisciplinario,
    };

    const serviceMap: Record<string, () => void> = {
      fundado: () => this.ttaipService.declararFundado(apelacionId, request).subscribe({ next: () => this.exito('Apelacion declarada FUNDADA'), error: (err) => this.errorHandler(err) }),
      fundado_en_parte: () => this.ttaipService.declararFundadoEnParte(apelacionId, request).subscribe({ next: () => this.exito('Apelacion declarada FUNDADA EN PARTE'), error: (err) => this.errorHandler(err) }),
      infundado: () => this.ttaipService.declararInfundado(apelacionId, { fundamentos: this.fundamentos.trim() }).subscribe({ next: () => this.exito('Apelacion declarada INFUNDADA'), error: (err) => this.errorHandler(err) }),
      infundado_en_parte: () => this.ttaipService.declararInfundadoEnParte(apelacionId, { fundamentos: this.fundamentos.trim() }).subscribe({ next: () => this.exito('Apelacion declarada INFUNDADA EN PARTE'), error: (err) => this.errorHandler(err) }),
      improcedente: () => this.ttaipService.declararImprocedente(apelacionId, { fundamentos: this.fundamentos.trim() }).subscribe({ next: () => this.exito('Apelacion declarada IMPROCEDENTE'), error: (err) => this.errorHandler(err) }),
      sustraccion_materia: () => this.ttaipService.declararSustraccionMateria(apelacionId, { fundamentos: this.fundamentos.trim() }).subscribe({ next: () => this.exito('Conclusion por sustraccion de la materia'), error: (err) => this.errorHandler(err) }),
      desistimiento: () => this.ttaipService.declararDesistimiento(apelacionId, { fundamentos: this.fundamentos.trim() }).subscribe({ next: () => this.exito('Conclusion por desistimiento'), error: (err) => this.errorHandler(err) }),
    };

    const handler = serviceMap[this.decision];
    if (handler) {
      handler();
      return;
    }

    this.loading.set(false);
    this.error.set('Decision no valida');
  }

  private exito(msg: string): void {
    this.loading.set(false);
    this.mensaje.set(msg);
    setTimeout(() => this.router.navigate(['/ttaip']), 2000);
  }

  private errorHandler(err: any): void {
    this.loading.set(false);
    this.error.set(err?.error?.mensaje || 'Error al procesar la resolucion');
  }

  getDecisionLabel(): string {
    const labels: Record<string, string> = {
      fundado: 'FUNDADA',
      fundado_en_parte: 'FUNDADA EN PARTE',
      infundado: 'INFUNDADA',
      infundado_en_parte: 'INFUNDADA EN PARTE',
      improcedente: 'IMPROCEDENTE',
      sustraccion_materia: 'SUSTRACCION DE MATERIA',
      desistimiento: 'DESISTIMIENTO',
    };

    return labels[this.decision] || '';
  }
}
