import {
  ChangeDetectionStrategy,
  Component,
  PLATFORM_ID,
  computed,
  inject,
  signal,
} from '@angular/core';
import { DatePipe, isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Solicitud } from '../../models/solicitud.model';
import { CrearRespuestaRequest } from '../../models/respuesta.model';
import { RespuestaService } from '../../services/respuesta.service';
import { SolicitudService } from '../../services/solicitud.service';
import {
  canSendManualResponse,
  isSilencioAdministrativo,
} from '../../utils/silencio-administrativo.util';

type DecisionFormulario = 'ACEPTAR' | 'DENEGAR';

interface ArchivoPreview {
  nombre: string;
  tamano: string;
}

interface SesionFuncionario {
  funcionarioId?: number;
  usuarioId?: number;
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
  private readonly respuestaService = inject(RespuestaService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly loadingSolicitud = signal(true);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);
  readonly solicitud = signal<Solicitud | null>(null);
  readonly archivosAdjuntos = signal<File[]>([]);

  readonly causalesDenegacion = signal<string[]>([]);
  private readonly causalesFallback = [
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

    return canSendManualResponse(this.construirContexto(solicitud));
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
    this.cargarCausalesDenegacion();
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

    const solicitud = this.solicitud();
    if (!solicitud) {
      this.error.set('No se encontro la solicitud para registrar la respuesta.');
      return;
    }

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

    const funcionarioId = this.obtenerFuncionarioIdDesdeSesion();
    if (funcionarioId === null) {
      this.error.set('No se pudo obtener el funcionario desde la sesion.');
      return;
    }

    this.loading.set(true);

    const contenido = this.formulario.controls.contenido.value.trim();
    const decision = this.formulario.controls.decision.value;

    if (decision === 'ACEPTAR') {
      const payload: CrearRespuestaRequest = {
        solicitudId: solicitud.idSolicitud,
        funcionarioId,
        tipoRespuesta: 'ENTREGA_TOTAL',
        contenido,
        formatoEntrega: 'DIGITAL',
        plazoEntrega: 5,
      };

      this.respuestaService.aceptarSolicitud(payload).subscribe({
        next: () => {
          this.loading.set(false);
          this.mensaje.set('Respuesta de aceptacion enviada correctamente.');
        },
        error: (errorResponse) => {
          this.loading.set(false);
          const mensaje = errorResponse?.error?.mensaje as string | undefined;
          this.error.set(mensaje ?? 'No se pudo enviar la respuesta de aceptacion.');
        },
      });
      return;
    }

    const payload: CrearRespuestaRequest = {
      solicitudId: solicitud.idSolicitud,
      funcionarioId,
      tipoRespuesta: 'DENEGACION_TOTAL',
      contenido,
      causalDenegatoria: this.formulario.controls.causalDenegatoria.value?.trim() || null,
      fundamentoLegal: this.formulario.controls.fundamentoLegal.value?.trim() || null,
    };

    this.respuestaService.denegarSolicitud(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.mensaje.set('Respuesta de denegacion enviada correctamente.');
      },
      error: (errorResponse) => {
        this.loading.set(false);
        const mensaje = errorResponse?.error?.mensaje as string | undefined;
        this.error.set(mensaje ?? 'No se pudo enviar la respuesta de denegacion.');
      },
    });
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

  private cargarCausalesDenegacion(): void {
    this.respuestaService.getCausalesDenegacion().subscribe({
      next: (causales) => {
        this.causalesDenegacion.set(causales.length > 0 ? causales : this.causalesFallback);
      },
      error: () => {
        this.causalesDenegacion.set(this.causalesFallback);
      },
    });
  }

  private tieneRespuesta(solicitud: Solicitud): boolean {
    if (solicitud.respuesta?.tipoRespuesta) {
      return true;
    }

    return solicitud.estado === 'RESPONDIDA' || solicitud.estado === 'DENEGADA' || solicitud.estado === 'CONCLUIDA';
  }

  private esSilencioAdministrativo(solicitud: Solicitud): boolean {
    const porSemaforo = solicitud.semaforo === 'NEGRO' && !solicitud.respuesta?.tipoRespuesta;
    return isSilencioAdministrativo(this.construirContexto(solicitud)) || porSemaforo;
  }

  private construirContexto(solicitud: Solicitud) {
    return {
      estado: solicitud.estado,
      diasRestantes: solicitud.diasRestantes,
      respuesta: solicitud.respuesta
        ? {
            tipoRespuesta: solicitud.respuesta.tipoRespuesta,
          }
        : null,
    };
  }

  private obtenerFuncionarioIdDesdeSesion(): number | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const rawSesion = localStorage.getItem('usuario');
    if (!rawSesion) {
      return null;
    }

    try {
      const sesion = JSON.parse(rawSesion) as SesionFuncionario;
      if (typeof sesion.funcionarioId === 'number') {
        return sesion.funcionarioId;
      }
      if (typeof sesion.usuarioId === 'number') {
        return sesion.usuarioId;
      }
      return null;
    } catch {
      return null;
    }
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
