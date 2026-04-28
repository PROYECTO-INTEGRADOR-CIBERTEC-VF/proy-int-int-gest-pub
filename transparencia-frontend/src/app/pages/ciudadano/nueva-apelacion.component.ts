import { ChangeDetectionStrategy, Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Solicitud } from '../../models/solicitud.model';
import { SolicitudService } from '../../services/solicitud.service';
import { ApelacionService } from '../../services/apelacion.service';

interface SesionCiudadano {
  ciudadanoId?: number;
}

@Component({
  selector: 'app-nueva-apelacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './nueva-apelacion.component.html',
  styleUrl: './nueva-apelacion.component.css',
})
export class NuevaApelacionComponent {
  private readonly fb = inject(FormBuilder);
  private readonly solicitudService = inject(SolicitudService);
  private readonly apelacionService = inject(ApelacionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly platformId = inject(PLATFORM_ID);

  readonly solicitudesApelables = signal<Solicitud[]>([]);
  readonly loadingSolicitudes = signal(true);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);
  readonly expedienteGenerado = signal<string | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    solicitudId: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
    fundamentos: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(50)]),
  });

  readonly solicitudSeleccionada = computed(() => {
    const id = this.formulario.controls.solicitudId.value;
    return this.solicitudesApelables().find((s) => (s.idSolicitud ?? (s as any).id) === id) ?? null;
  });

  readonly fundamentosLength = computed(() => this.formulario.controls.fundamentos.value.length);

  constructor() {
    this.cargarSolicitudesApelables();

    // Si viene con queryParam solicitudId, seleccionarla
    const solicitudIdParam = this.route.snapshot.queryParamMap.get('solicitudId');
    if (solicitudIdParam) {
      const id = Number(solicitudIdParam);
      if (!isNaN(id) && id > 0) {
        this.formulario.controls.solicitudId.setValue(id);
      }
    }
  }

  cargarSolicitudesApelables(): void {
    const ciudadanoId = this.obtenerCiudadanoIdDesdeSesion();
    if (!ciudadanoId) {
      this.loadingSolicitudes.set(false);
      this.error.set('No se encontro ciudadano en sesion.');
      return;
    }

    this.solicitudService.findByCiudadanoId(ciudadanoId).subscribe({
      next: (solicitudes) => {
        // Filtrar solo solicitudes que pueden ser apeladas
        const apelables = solicitudes.filter((s) =>
          s.estado === 'RESPONDIDA' || s.estado === 'DENEGADA' || s.estado === 'VENCIDA'
        );
        this.solicitudesApelables.set(apelables);
        this.loadingSolicitudes.set(false);
      },
      error: () => {
        this.loadingSolicitudes.set(false);
        this.error.set('No se pudieron cargar las solicitudes.');
      },
    });
  }

  enviarApelacion(): void {
    this.error.set(null);
    this.mensaje.set(null);
    this.expedienteGenerado.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      if (this.formulario.controls.solicitudId.invalid) {
        this.error.set('Debe seleccionar una solicitud SAIP para apelar.');
      } else if (this.formulario.controls.fundamentos.invalid) {
        this.error.set('Los fundamentos deben tener al menos 50 caracteres.');
      }
      return;
    }

    const ciudadanoId = this.obtenerCiudadanoIdDesdeSesion();
    if (!ciudadanoId) {
      this.error.set('No se encontro ciudadano en sesion. Inicie sesion nuevamente.');
      return;
    }

    this.submitting.set(true);

    const formValue = this.formulario.getRawValue();
    // Select values come as strings - convert to number
    const solicitudId = Number(formValue.solicitudId);

    if (!solicitudId || isNaN(solicitudId)) {
      this.error.set('No se pudo determinar el ID de la solicitud.');
      this.submitting.set(false);
      return;
    }

    this.apelacionService.crear({
      solicitudId,
      ciudadanoId,
      fundamentos: formValue.fundamentos.trim(),
    }).subscribe({
      next: (apelacion) => {
        this.submitting.set(false);
        this.expedienteGenerado.set(apelacion.expediente);
        this.mensaje.set('Apelacion registrada exitosamente.');

        setTimeout(() => {
          void this.router.navigate(['/ciudadano/dashboard']);
        }, 3000);
      },
      error: (errorResponse) => {
        this.submitting.set(false);
        const msg = errorResponse?.error?.error as string | undefined;
        this.error.set(msg ?? 'No se pudo registrar la apelacion.');
      },
    });
  }

  campoInvalido(nombre: 'solicitudId' | 'fundamentos'): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && control.touched;
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
      const sesion = JSON.parse(raw) as SesionCiudadano;
      return typeof sesion.ciudadanoId === 'number' ? sesion.ciudadanoId : null;
    } catch {
      return null;
    }
  }
}
