import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, signal, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegistroRequest } from '../../models/usuario.model';

function passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
  const value = String(control.value ?? '');

  if (!value) {
    return null;
  }

  const hasUppercase = /[A-Z]/.test(value);
  const hasNumber = /\d/.test(value);

  return hasUppercase && hasNumber ? null : { passwordStrength: true };
}

function passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = String(group.get('password')?.value ?? '');
  const confirmarPassword = String(group.get('confirmarPassword')?.value ?? '');

  if (!password || !confirmarPassword) {
    return null;
  }

  return password === confirmarPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-registro',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly pasoActual = signal<1 | 2>(1);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly form = this.formBuilder.nonNullable.group(
    {
      dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      nombreCompleto: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6), passwordStrengthValidator]],
      confirmarPassword: ['', [Validators.required]],
      telefono: [''],
      direccion: ['']
    },
    { validators: [passwordMatchValidator] }
  );

  get dniControl() {
    return this.form.controls.dni;
  }

  get nombreCompletoControl() {
    return this.form.controls.nombreCompleto;
  }

  get emailControl() {
    return this.form.controls.email;
  }

  get passwordControl() {
    return this.form.controls.password;
  }

  get confirmarPasswordControl() {
    return this.form.controls.confirmarPassword;
  }

  onDniInput(value: string): void {
    const cleaned = value.replace(/\D/g, '').slice(0, 8);
    this.dniControl.setValue(cleaned, { emitEvent: false });
  }

  irPasoDos(): void {
    this.errorMessage.set('');

    if (this.dniControl.invalid || this.nombreCompletoControl.invalid) {
      this.dniControl.markAsTouched();
      this.nombreCompletoControl.markAsTouched();
      return;
    }

    this.pasoActual.set(2);
  }

  volverPasoUno(): void {
    this.errorMessage.set('');
    this.pasoActual.set(1);
  }

  onRegistrar(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.pasoActual() === 1) {
      this.irPasoDos();
      return;
    }

    if (this.form.invalid) {
      this.emailControl.markAsTouched();
      this.passwordControl.markAsTouched();
      this.confirmarPasswordControl.markAsTouched();
      return;
    }

    const payload = this.buildPayload();

    this.loading.set(true);

    this.authService.registro(payload).subscribe({
      next: (response) => {
        this.loading.set(false);

        if (!response.success) {
          this.errorMessage.set(response.mensaje ?? 'No se pudo completar el registro.');
          return;
        }

        this.successMessage.set('Registro exitoso. Redirigiendo al acceso ciudadano...');
        this.form.reset({
          dni: '',
          nombreCompleto: '',
          email: '',
          password: '',
          confirmarPassword: '',
          telefono: '',
          direccion: ''
        });
        this.pasoActual.set(1);

        setTimeout(() => {
          void this.router.navigate(['/login']);
        }, 1200);
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.errorMessage.set(this.extractErrorMessage(error));
      }
    });
  }

  dniError(): string {
    if (!this.dniControl.touched) {
      return '';
    }

    if (this.dniControl.hasError('required')) {
      return 'El DNI es obligatorio.';
    }

    if (this.dniControl.hasError('pattern')) {
      return 'El DNI debe tener exactamente 8 dígitos.';
    }

    return '';
  }

  nombreError(): string {
    if (!this.nombreCompletoControl.touched) {
      return '';
    }

    if (this.nombreCompletoControl.hasError('required')) {
      return 'El nombre completo es obligatorio.';
    }

    if (this.nombreCompletoControl.hasError('minlength')) {
      return 'Ingrese nombres y apellidos válidos.';
    }

    return '';
  }

  emailError(): string {
    if (!this.emailControl.touched) {
      return '';
    }

    if (this.emailControl.hasError('required')) {
      return 'El correo es obligatorio.';
    }

    if (this.emailControl.hasError('email')) {
      return 'Ingrese un correo válido.';
    }

    return '';
  }

  passwordError(): string {
    if (!this.passwordControl.touched) {
      return '';
    }

    if (this.passwordControl.hasError('required')) {
      return 'La contraseña es obligatoria.';
    }

    if (this.passwordControl.hasError('minlength')) {
      return 'La contraseña debe tener al menos 6 caracteres.';
    }

    if (this.passwordControl.hasError('passwordStrength')) {
      return 'La contraseña debe incluir 1 mayúscula y 1 número.';
    }

    return '';
  }

  confirmarPasswordError(): string {
    if (!this.confirmarPasswordControl.touched) {
      return '';
    }

    if (this.confirmarPasswordControl.hasError('required')) {
      return 'Debe confirmar la contraseña.';
    }

    if (this.form.hasError('passwordMismatch')) {
      return 'Las contraseñas no coinciden.';
    }

    return '';
  }

  cumpleLongitud(): boolean {
    return this.passwordControl.value.length >= 6;
  }

  cumpleMayuscula(): boolean {
    return /[A-Z]/.test(this.passwordControl.value);
  }

  cumpleNumero(): boolean {
    return /\d/.test(this.passwordControl.value);
  }

  private buildPayload(): RegistroRequest {
    const data = this.form.getRawValue();

    return {
      dni: data.dni,
      nombreCompleto: data.nombreCompleto.trim(),
      email: data.email.trim(),
      password: data.password,
      telefono: data.telefono.trim() || undefined,
      direccion: data.direccion.trim() || undefined
    };
  }

  private extractErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const payload = (error as { error?: { mensaje?: string } }).error;
      if (payload?.mensaje) {
        return payload.mensaje;
      }
    }

    return 'No se pudo registrar al ciudadano. Verifique los datos e intente nuevamente.';
  }
}
