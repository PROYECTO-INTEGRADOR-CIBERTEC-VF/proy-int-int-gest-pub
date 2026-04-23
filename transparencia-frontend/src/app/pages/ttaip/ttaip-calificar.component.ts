import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Apelacion } from '../../models/apelacion.model';
import { ApelacionService } from '../../services/apelacion.service';
import { TtaipService } from '../../services/ttaip.service';

type DecisionCalificacion = 'admitir' | 'subsanar' | 'inadmitir' | '';

@Component({
  selector: 'app-ttaip-calificar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ttaip-calificar.component.html',
  styleUrl: './ttaip-calificar.component.css'
})
export class TtaipCalificarComponent implements OnInit {
  expediente = '';

  decision: DecisionCalificacion = '';
  fundamentos = '';
  observaciones = '';
  diasSubsanacion = 2;

  readonly apelacion = signal<Apelacion | null>(null);
  readonly loadingApelacion = signal<boolean>(false);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly mensaje = signal<string>('');

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly apelacionService: ApelacionService,
    private readonly ttaipService: TtaipService
  ) {}

  ngOnInit(): void {
    const expedienteParam = this.route.snapshot.paramMap.get('expediente');
    if (!expedienteParam) {
      this.error.set('No se encontro el expediente para calificar.');
      return;
    }

    this.expediente = expedienteParam;
    this.cargarApelacion(expedienteParam);
  }

  esSubsanacion(): boolean {
    return this.decision === 'subsanar';
  }

  private cargarApelacion(expediente: string): void {
    this.loadingApelacion.set(true);
    this.error.set('');

    this.apelacionService.findByExpediente(expediente).subscribe({
      next: (apelacion) => {
        this.apelacion.set(apelacion);
        this.expediente = apelacion.expediente;
        this.loadingApelacion.set(false);
      },
      error: () => {
        this.loadingApelacion.set(false);
        this.error.set('No se pudo cargar la apelacion para calificar.');
      }
    });
  }

  registrarCalificacion(): void {
    if (this.loading()) {
      return;
    }

    this.error.set('');
    this.mensaje.set('');

    const apelacionActual = this.apelacion();
    if (!apelacionActual) {
      this.error.set('No hay apelacion cargada para procesar.');
      return;
    }

    const fundamentosLimpios = this.fundamentos.trim();
    const observacionesLimpias = this.observaciones.trim();

    if (!this.decision) {
      this.error.set('Debe seleccionar una decision de calificacion.');
      return;
    }

    if (!fundamentosLimpios) {
      this.error.set('Los fundamentos son obligatorios.');
      return;
    }

    if (this.decision === 'subsanar' && !observacionesLimpias) {
      this.error.set('Las observaciones son obligatorias cuando se requiere subsanacion.');
      return;
    }

    if (this.decision === 'subsanar' && this.diasSubsanacion < 1) {
      this.error.set('El plazo de subsanacion debe ser mayor o igual a 1 dia habil.');
      return;
    }

    this.loading.set(true);

    const requestBase = {
      fundamentos: fundamentosLimpios
    };

    if (this.decision === 'admitir') {
      this.ttaipService.admitirApelacion(apelacionActual.idApelacion, requestBase).subscribe({
        next: () => this.procesarExito('Apelacion admitida correctamente.'),
        error: (err: any) => this.procesarError(err)
      });
      return;
    }

    if (this.decision === 'subsanar') {
      this.ttaipService.requerirSubsanacion(apelacionActual.idApelacion, {
        ...requestBase,
        observaciones: observacionesLimpias,
        diasSubsanacion: this.diasSubsanacion
      }).subscribe({
        next: () => this.procesarExito('Subsanacion requerida correctamente.'),
        error: (err: any) => this.procesarError(err)
      });
      return;
    }

    this.ttaipService.inadmitirApelacion(apelacionActual.idApelacion, requestBase).subscribe({
      next: () => this.procesarExito('Apelacion inadmitida correctamente.'),
      error: (err: any) => this.procesarError(err)
    });
  }

  private procesarExito(mensaje: string): void {
    this.loading.set(false);
    this.mensaje.set(mensaje);
    setTimeout(() => {
      this.router.navigate(['/ttaip']);
    }, 1800);
  }

  private procesarError(err: any): void {
    this.loading.set(false);
    this.error.set(err?.error?.mensaje ?? 'No se pudo registrar la calificacion.');
  }
}
