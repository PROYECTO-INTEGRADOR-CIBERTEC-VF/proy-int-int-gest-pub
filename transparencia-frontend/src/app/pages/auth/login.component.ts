import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest, LoginResponse, TipoUsuario } from '../../models/usuario.model';

type AccessMode = 'citizen' | 'internal';

@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly formBuilder = inject(FormBuilder);

  readonly form = this.formBuilder.nonNullable.group({
    identificador: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly accessMode = signal<AccessMode>('citizen');
  readonly requiredRole = signal<TipoUsuario | null>(null);

  ngOnInit(): void {
    const data = this.route.snapshot.data as Partial<{ accessMode: AccessMode; requiredRole: TipoUsuario }>;

    this.accessMode.set(data.accessMode === 'internal' ? 'internal' : 'citizen');
    this.requiredRole.set(data.requiredRole ?? null);

    this.configureIdentificadorValidators();

    if (this.route.snapshot.queryParamMap.get('registered') === '1') {
      this.successMessage.set('Registro exitoso. Inicie sesión para continuar.');
    }

    this.redirectIfSessionMatchesRoute();
  }

  get identificadorControl() {
    return this.form.controls.identificador;
  }

  get passwordControl() {
    return this.form.controls.password;
  }

  identificadorLabel(): string {
    return this.accessMode() === 'citizen' ? 'DNI' : 'Correo institucional';
  }

  identificadorPlaceholder(): string {
    return this.accessMode() === 'citizen' ? 'Ingrese su DNI (8 dígitos)' : 'usuario@institucion.gob.pe';
  }

  requiredRoleLabel(): string {
    const role = this.requiredRole();

    switch (role) {
      case 'FUNCIONARIO':
        return 'Funcionario';
      case 'TTAIP':
        return 'Miembro TTAIP';
      case 'ADMINISTRADOR':
        return 'Administrador';
      default:
        return 'Usuario interno';
    }
  }

  onIdentificadorInput(value: string): void {
    if (this.accessMode() !== 'citizen') {
      return;
    }

    const cleaned = value.replace(/\D/g, '').slice(0, 8);
    this.identificadorControl.setValue(cleaned, { emitEvent: false });
  }

  identificadorError(): string {
    if (!this.identificadorControl.touched) {
      return '';
    }

    if (this.identificadorControl.hasError('required')) {
      return 'El identificador es obligatorio.';
    }

    if (this.accessMode() === 'citizen' && this.identificadorControl.hasError('pattern')) {
      return 'El DNI debe contener exactamente 8 dígitos.';
    }

    if (this.accessMode() === 'internal' && this.identificadorControl.hasError('email')) {
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

    return '';
  }

  onSubmit(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const credentials: LoginRequest = this.form.getRawValue();

    this.loading.set(true);

    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.loading.set(false);

        if (!response.success || !response.token) {
          this.errorMessage.set(response.mensaje ?? 'No se pudo iniciar sesión.');
          return;
        }

        const role = this.toRole(response.tipoUsuario);
        const expectedRole = this.requiredRole();

        if (expectedRole && role !== expectedRole) {
          this.errorMessage.set(`Este acceso es exclusivo para perfil ${this.requiredRoleLabel()}.`);
          this.authService.cerrarSesion();
          return;
        }

        this.authService.guardarSesion(response);
        void this.router.navigateByUrl(this.resolveTargetRoute(response, role));
      },
      error: (error: unknown) => {
        this.loading.set(false);
        this.errorMessage.set(this.extractErrorMessage(error));
      }
    });
  }

  private configureIdentificadorValidators(): void {
    if (this.accessMode() === 'citizen') {
      this.identificadorControl.setValidators([
        Validators.required,
        Validators.pattern(/^\d{8}$/)
      ]);
    } else {
      this.identificadorControl.setValidators([
        Validators.required,
        Validators.email
      ]);
    }

    this.identificadorControl.updateValueAndValidity({ emitEvent: false });
  }

  private redirectIfSessionMatchesRoute(): void {
    const sesion = this.authService.obtenerSesion();

    if (!sesion?.token) {
      return;
    }

    const role = this.toRole(sesion.tipoUsuario);
    const expectedRole = this.requiredRole();

    if (expectedRole && role !== expectedRole) {
      return;
    }

    void this.router.navigateByUrl(this.resolveTargetRoute(sesion, role));
  }

  private resolveTargetRoute(response: LoginResponse, role: TipoUsuario | null): string {
    const backendRouteMap: Record<string, string> = {
      '/ciudadano/dashboard': '/ciudadano',
      '/entidad/dashboard': '/funcionario',
      '/ttaip/dashboard': '/ttaip',
      '/admin/dashboard': '/admin'
    };

    if (response.redirectUrl && backendRouteMap[response.redirectUrl]) {
      return backendRouteMap[response.redirectUrl];
    }

    const roleRouteMap: Record<TipoUsuario, string> = {
      CIUDADANO: '/ciudadano',
      FUNCIONARIO: '/funcionario',
      TTAIP: '/ttaip',
      ADMINISTRADOR: '/admin'
    };

    if (role) {
      return roleRouteMap[role];
    }

    return '/login';
  }

  private toRole(value: string | TipoUsuario | undefined): TipoUsuario | null {
    if (!value) {
      return null;
    }

    const normalized = String(value).toUpperCase();

    switch (normalized) {
      case 'CIUDADANO':
      case 'FUNCIONARIO':
      case 'TTAIP':
      case 'ADMINISTRADOR':
        return normalized;
      default:
        return null;
    }
  }

  private extractErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const payload = (error as { error?: { mensaje?: string } }).error;
      if (payload?.mensaje) {
        return payload.mensaje;
      }
    }

    return 'Credenciales inválidas o servicio no disponible.';
  }
}
