import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Apelacion } from '../../models/apelacion.model';
import { ApelacionService } from '../../services/apelacion.service';

@Component({
  selector: 'app-subsanacion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './subsanacion.component.html'
})
export class SubsanacionComponent implements OnInit {
  apelacion = signal<Apelacion | null>(null);
  fundamentosAdicionales = signal<string>('');
  cargando = signal<boolean>(true);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apelacionService: ApelacionService
  ) {}

  ngOnInit() {
    const expediente = this.route.snapshot.paramMap.get('expediente');
    if (expediente) {
      this.cargarApelacion(expediente);
    }
  }

  cargarApelacion(expediente: string) {
    this.apelacionService.findByExpediente(expediente).subscribe({
      next: (data) => {
        this.apelacion.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
      }
    });
  }

  obtenerDiasRestantes(): number {
    const ap = this.apelacion();
    return ap?.diasRestantes ?? 0;
  }

  enviarSubsanacion() {
    const id = this.apelacion()?.idApelacion;
    const nuevosFundamentos = this.fundamentosAdicionales();

    if (id && nuevosFundamentos.trim().length > 0) {
      this.apelacionService.subsanar(id, nuevosFundamentos).subscribe({
        next: () => {
          this.router.navigate(['/ciudadano/dashboard']);
        },
        error: (err) => {
          console.error(err);
        }
      });
    }
  }
}