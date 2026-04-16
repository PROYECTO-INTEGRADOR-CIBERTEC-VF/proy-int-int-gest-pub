import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApelacionService } from '../../services/apelacion.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-nueva-apelacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './nueva-apelacion.component.html'
})
export default class NuevaApelacionComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private apelacionService = inject(ApelacionService);
  private authService = inject(AuthService);

  // Signals para manejar el estado visual
  modo = signal<'expediente' | 'solicitud' | 'seleccion'>('seleccion');
  solicitudId = signal<number | null>(null);
  expediente = signal<string | null>(null);
  enviando = signal<boolean>(false);

  apelacionForm = this.fb.group({
    fundamentos: ['', [Validators.required, Validators.minLength(50)]]
  });

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const exp = params.get('expediente');
      if (exp) {
        this.modo.set('expediente');
        this.expediente.set(exp);
      }
    });

    this.route.queryParamMap.subscribe(params => {
      const solId = params.get('solicitudId');
      if (solId) {
        this.modo.set('solicitud');
        this.solicitudId.set(Number(solId));
      }
    });
  }

  onSubmit() {
    if (this.apelacionForm.invalid) {
      this.apelacionForm.markAllAsTouched();
      return;
    }

    const sesion = this.authService.obtenerSesion();
    if (!sesion) return;

    this.enviando.set(true);
    
    // Armamos el payload para el backend
    const datos = {
      solicitudId: this.solicitudId(),
      ciudadanoId: sesion.ciudadanoId,
      fundamentos: this.apelacionForm.value.fundamentos!
    };

    this.apelacionService.crear(datos).subscribe({
      next: (res) => {
        alert(`¡Apelación registrada con éxito! Tu N° de Expediente es: ${res.expediente}`);
        this.router.navigate(['/ciudadano']);
      },
      error: () => {
        alert('Ocurrió un error al registrar la apelación. Intenta de nuevo.');
        this.enviando.set(false);
      }
    });
  }
}