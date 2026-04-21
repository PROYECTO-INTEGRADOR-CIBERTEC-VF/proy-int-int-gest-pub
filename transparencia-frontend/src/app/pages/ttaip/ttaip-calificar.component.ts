import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

type DecisionCalificacion = 'admitir' | 'subsanar' | 'inadmitir' | '';

interface CalificacionPayload {
  expediente: string;
  decision: 'admitir' | 'subsanar' | 'inadmitir';
  fundamentos: string;
  observaciones?: string;
  diasSubsanacion?: number;
}

@Component({
  selector: 'app-ttaip-calificar',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ttaip-calificar.component.html',
  styleUrl: './ttaip-calificar.component.css'
})
export class TtaipCalificarComponent implements OnInit {
  expediente = 'SIN-EXPEDIENTE';

  decision: DecisionCalificacion = '';
  fundamentos = '';
  observaciones = '';
  diasSubsanacion = 2;

  readonly error = signal<string>('');
  readonly mensaje = signal<string>('');
  readonly payloadPreview = signal<CalificacionPayload | null>(null);

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    this.expediente = this.route.snapshot.paramMap.get('expediente') ?? 'SIN-EXPEDIENTE';
  }

  esSubsanacion(): boolean {
    return this.decision === 'subsanar';
  }

  registrarCalificacion(): void {
    this.error.set('');
    this.mensaje.set('');
    this.payloadPreview.set(null);

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

    const payload: CalificacionPayload = {
      expediente: this.expediente,
      decision: this.decision,
      fundamentos: fundamentosLimpios
    };

    if (this.decision === 'subsanar') {
      payload.observaciones = observacionesLimpias;
      payload.diasSubsanacion = this.diasSubsanacion;
    }

    this.payloadPreview.set(payload);
    this.mensaje.set('Formulario de primera calificacion validado. Listo para integracion en FE-03.');
  }
}
