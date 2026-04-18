import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Solicitud } from '../../models/solicitud.model';
import { SolicitudService } from '../../services/solicitud.service';

type DecisionFormulario = 'ACEPTAR' | 'DENEGAR';

interface ArchivoPreview {
  nombre: string;
  tamano: string;
}

@Component({
  selector: 'app-funcionario-responder',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './funcionario-responder.component.html',
  styleUrl: './funcionario-responder.component.css',
})
export class FuncionarioResponderComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly solicitudService = inject(SolicitudService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly loadingSolicitud = signal(true);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);
  readonly solicitud = signal<Solicitud | null>(null);
  readonly archivosAdjuntos = signal<File[]>([]);

  readonly causalesDenegacion = [
    'Informacion confidencial',
    'Informacion reservada',
    'Informacion secreta',
    'Proteccion de datos personales',
    'Seguridad nacional',
  ];

  readonly formulario = this.fb.nonNullable.group({
    decision: this.fb.nonNullable.control<DecisionFormulario>('ACEPTAR'),
    contenido: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(20)]),
    causalDenegatoria: this.fb.control(''),
    fundamentoLegal: this.fb.control(''),
  });

  readonly mostrarCamposDenegacion = computed(
    () => this.formulario.controls.decision.value === 'DENEGAR',
  );

  readonly puedeRegistrarRespuesta = computed(() => {
    const solicitud = this.solicitud();
    if (!solicitud) {
      return false;
    }

    return !this.esSilencioAdministrativo(solicitud) && !this.tieneRespuesta(solicitud);
  });

  readonly previewArchivos = computed<ArchivoPreview[]>(() =>
    this.archivosAdjuntos().map((archivo) => ({
      nombre: archivo.name,
      tamano: this.formatearTamano(archivo.size),
    })),
  );

  constructor() {
    this.formulario.controls.decision.valueChanges.subscribe((decision) => {
      this.actualizarValidacionesSegunDecision(decision);
    });

    this.actualizarValidacionesSegunDecision(this.formulario.controls.decision.value);
    this.cargarSolicitudDesdeRuta();
  }

  onSeleccionArchivos(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    this.archivosAdjuntos.set(files);
  }

  quitarArchivo(index: number): void {
    const archivos = [...this.archivosAdjuntos()];
    archivos.splice(index, 1);
    this.archivosAdjuntos.set(archivos);
  }

  enviarRespuesta(): void {
    this.error.set(null);
    this.mensaje.set(null);

    if (!this.puedeRegistrarRespuesta()) {
      this.error.set('No se permite registrar respuesta manual para esta solicitud.');
      return;
    }

    this.formulario.markAllAsTouched();

    if (this.archivosAdjuntos().length === 0) {
      this.error.set('Debe adjuntar al menos un archivo para la respuesta.');
      return;
    }

    if (this.formulario.invalid) {
      this.error.set('Complete los campos obligatorios antes de enviar la respuesta.');
      return;
    }

    this.loading.set(true);

    setTimeout(() => {
      this.loading.set(false);
      this.mensaje.set(
        this.formulario.controls.decision.value === 'ACEPTAR'
          ? 'Respuesta de aceptacion validada. Lista para integracion con API en FE-03.'
          : 'Respuesta de denegacion validada. Lista para integracion con API en FE-03.',
      );
    }, 450);
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

  campoInvalido(nombre: 'contenido' | 'causalDenegatoria' | 'fundamentoLegal'): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && control.touched;
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

  private actualizarValidacionesSegunDecision(decision: DecisionFormulario): void {
    const causal = this.formulario.controls.causalDenegatoria;
    const fundamento = this.formulario.controls.fundamentoLegal;

    if (decision === 'DENEGAR') {
      causal.setValidators([Validators.required]);
      fundamento.setValidators([Validators.required, Validators.minLength(30)]);
    } else {
      causal.clearValidators();
      fundamento.clearValidators();
      causal.setValue('');
      fundamento.setValue('');
    }

    causal.updateValueAndValidity({ emitEvent: false });
    fundamento.updateValueAndValidity({ emitEvent: false });
  }

  private cargarSolicitudDesdeRuta(): void {
    const expediente = this.route.snapshot.paramMap.get('expediente');
    if (!expediente) {
      this.loadingSolicitud.set(false);
      this.error.set('No se recibio expediente para responder.');
      return;
    }

    this.loadingSolicitud.set(true);
    this.error.set(null);

    this.solicitudService.findByExpediente(expediente).subscribe({
      next: (solicitud) => {
        this.solicitud.set(solicitud);
        this.loadingSolicitud.set(false);
      },
      error: () => {
        this.loadingSolicitud.set(false);
        this.error.set('No se pudo cargar la solicitud para registrar la respuesta.');
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

  private formatearTamano(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }

    const kb = bytes / 1024;
    if (kb < 1024) {
      return `${kb.toFixed(1)} KB`;
    }

    const mb = kb / 1024;
    return `${mb.toFixed(1)} MB`;
  }
}
