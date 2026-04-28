import { ChangeDetectionStrategy, Component, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Apelacion } from '../../models/apelacion.model';
import { ApelacionService } from '../../services/apelacion.service';

interface SesionCiudadano {
  ciudadanoId?: number;
}

@Component({
  selector: 'app-subsanacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './subsanacion.component.html',
  styleUrl: './subsanacion.component.css',
})
export class SubsanacionComponent {
  private readonly fb = inject(FormBuilder);
  private readonly apelacionService = inject(ApelacionService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly platformId = inject(PLATFORM_ID);

  readonly apelacionesSubsanables = signal<Apelacion[]>([]);
  readonly loadingApelaciones = signal(true);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);

  readonly formulario = this.fb.nonNullable.group({
    apelacionId: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
    fundamentosAdicionales: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(30)]),
  });

  readonly apelacionSeleccionada = computed(() => {
    const id = this.formulario.controls.apelacionId.value;
    return this.apelacionesSubsanables().find((a) => (a.idApelacion ?? (a as any).id) === id) ?? null;
  });

  readonly fundamentosLength = computed(() => this.formulario.controls.fundamentosAdicionales.value.length);

  constructor() {
    this.cargarApelacionesSubsanables();

    // Si viene con queryParam apelacionId
    const apelacionIdParam = this.route.snapshot.queryParamMap.get('apelacionId');
    if (apelacionIdParam) {
      const id = Number(apelacionIdParam);
      if (!isNaN(id) && id > 0) {
        this.formulario.controls.apelacionId.setValue(id);
      }
    }
  }

  cargarApelacionesSubsanables(): void {
    const ciudadanoId = this.obtenerCiudadanoIdDesdeSesion();
    if (!ciudadanoId) {
      this.loadingApelaciones.set(false);
      this.error.set('No se encontro ciudadano en sesion.');
      return;
    }

    this.apelacionService.findByCiudadanoId(ciudadanoId).subscribe({
      next: (apelaciones) => {
        const subsanables = apelaciones.filter((a) => a.estado === 'EN_SUBSANACION');
        this.apelacionesSubsanables.set(subsanables);
        this.loadingApelaciones.set(false);

        // Si solo hay una, seleccionarla automaticamente
        if (subsanables.length === 1 && this.formulario.controls.apelacionId.value === 0) {
          this.formulario.controls.apelacionId.setValue(subsanables[0].idApelacion ?? (subsanables[0] as any).id);
        }
      },
      error: () => {
        this.loadingApelaciones.set(false);
        this.error.set('No se pudieron cargar las apelaciones.');
      },
    });
  }

  enviarSubsanacion(): void {
    this.error.set(null);
    this.mensaje.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      if (this.formulario.controls.apelacionId.invalid) {
        this.error.set('Debe seleccionar una apelacion para subsanar.');
      } else {
        this.error.set('Los fundamentos adicionales deben tener al menos 30 caracteres.');
      }
      return;
    }

    this.submitting.set(true);

    const formValue = this.formulario.getRawValue();

    this.apelacionService.subsanar(
      Number(formValue.apelacionId),
      formValue.fundamentosAdicionales.trim()
    ).subscribe({
      next: () => {
        this.submitting.set(false);
        this.mensaje.set('Subsanacion enviada exitosamente. Su apelacion pasara a segunda calificacion.');

        setTimeout(() => {
          void this.router.navigate(['/ciudadano/dashboard']);
        }, 3000);
      },
      error: (errorResponse) => {
        this.submitting.set(false);
        const msg = errorResponse?.error?.error as string | undefined;
        this.error.set(msg ?? 'No se pudo enviar la subsanacion.');
      },
    });
  }

  campoInvalido(nombre: 'apelacionId' | 'fundamentosAdicionales'): boolean {
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
