import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  PLATFORM_ID,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EntidadService } from '../../services/entidad.service';
import { SolicitudService } from '../../services/solicitud.service';
import { Entidad } from '../../models/entidad.model';
import { SolicitudCreate } from '../../models/solicitud.model';
import { FileUploadComponent } from '../../components/file-upload/file-upload.component';

interface SesionUsuario {
  ciudadanoId?: number;
  usuarioId?: number;
}

const alMenosUnFormatoSeleccionado: ValidatorFn = (group) => {
  const value = group.value as {
    formatoDigital?: boolean;
    formatoFisico?: boolean;
    copiaSimple?: boolean;
    copiaCertificada?: boolean;
  };

  const hasAtLeastOne =
    Boolean(value.formatoDigital) ||
    Boolean(value.formatoFisico) ||
    Boolean(value.copiaSimple) ||
    Boolean(value.copiaCertificada);

  return hasAtLeastOne ? null : ({ formatoRequerido: true } as ValidationErrors);
};

@Component({
  selector: 'app-nueva-saip',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, FileUploadComponent],
  templateUrl: './nueva-saip.component.html',
  styleUrl: './nueva-saip.component.css',
})
export class NuevaSaipComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly entidadService = inject(EntidadService);
  private readonly solicitudService = inject(SolicitudService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);

  private redirectTimeoutId: ReturnType<typeof setTimeout> | null = null;

  readonly entidades = signal<Entidad[]>([]);
  readonly loadingEntidades = signal(false);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly confirmacionVisible = signal(false);
  readonly expedienteGenerado = signal<string | null>(null);
  readonly adjuntos = signal<File[]>([]);

  readonly form = this.fb.group(
    {
      entidadId: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
      asunto: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
      descripcion: this.fb.nonNullable.control('', [Validators.required]),
      tipoInformacion: this.fb.nonNullable.control(''),
      formatoDigital: this.fb.nonNullable.control(true),
      formatoFisico: this.fb.nonNullable.control(false),
      copiaSimple: this.fb.nonNullable.control(true),
      copiaCertificada: this.fb.nonNullable.control(false),
    },
    { validators: alMenosUnFormatoSeleccionado },
  );

  readonly asuntoLength = computed(() => this.form.controls.asunto.value.length);

  constructor() {
    this.cargarEntidades();
  }

  cargarEntidades(): void {
    this.loadingEntidades.set(true);
    this.error.set(null);

    this.entidadService.findAll().subscribe({
      next: (entidades) => {
        this.entidades.set(entidades);
        this.loadingEntidades.set(false);
      },
      error: () => {
        this.loadingEntidades.set(false);
        this.error.set('No se pudieron cargar las entidades publicas.');
      },
    });
  }

  onSubmit(): void {
    this.error.set(null);
    this.confirmacionVisible.set(false);
    this.expedienteGenerado.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Complete los campos obligatorios para registrar la SAIP.');
      return;
    }

    const ciudadanoId = this.obtenerCiudadanoIdDesdeSesion();
    if (!ciudadanoId) {
      this.error.set('No se encontro ciudadano en sesion. Inicie sesion nuevamente.');
      return;
    }

    const formValue = this.form.getRawValue();
    const payload: SolicitudCreate = {
      asunto: formValue.asunto.trim(),
      descripcion: formValue.descripcion.trim(),
      ciudadanoId,
      entidadId: formValue.entidadId,
      tipoInformacion: formValue.tipoInformacion.trim(),
      formatoDigital: formValue.formatoDigital,
      formatoFisico: formValue.formatoFisico,
      copiaSimple: formValue.copiaSimple,
      copiaCertificada: formValue.copiaCertificada,
    };

    this.submitting.set(true);
    this.solicitudService.crear(payload).subscribe({
      next: (solicitud) => {
        this.submitting.set(false);
        this.expedienteGenerado.set(solicitud.expediente);
        this.confirmacionVisible.set(true);
        this.form.reset({
          entidadId: 0,
          asunto: '',
          descripcion: '',
          tipoInformacion: '',
          formatoDigital: true,
          formatoFisico: false,
          copiaSimple: true,
          copiaCertificada: false,
        });
        this.programarRedireccion();
      },
      error: (errorResponse) => {
        this.submitting.set(false);
        const mensaje = errorResponse?.error?.mensaje as string | undefined;
        this.error.set(mensaje ?? 'No se pudo registrar la solicitud SAIP.');
      },
    });
  }

  onFilesChanged(files: File[]): void {
    this.adjuntos.set(files);
  }

  private obtenerCiudadanoIdDesdeSesion(): number | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const raw = localStorage.getItem('usuario');
    if (!raw) {
      return null;
    }

    try {
      const sesion = JSON.parse(raw) as SesionUsuario;
      if (typeof sesion.ciudadanoId === 'number') {
        return sesion.ciudadanoId;
      }
      if (typeof sesion.usuarioId === 'number') {
        return sesion.usuarioId;
      }
      return null;
    } catch {
      return null;
    }
  }

  ngOnDestroy(): void {
    if (this.redirectTimeoutId) {
      clearTimeout(this.redirectTimeoutId);
      this.redirectTimeoutId = null;
    }
  }

  private programarRedireccion(): void {
    if (this.redirectTimeoutId) {
      clearTimeout(this.redirectTimeoutId);
    }

    this.redirectTimeoutId = setTimeout(() => {
      this.confirmacionVisible.set(false);
      this.router.navigate(['/ciudadano']);
    }, 2000);
  }
}
