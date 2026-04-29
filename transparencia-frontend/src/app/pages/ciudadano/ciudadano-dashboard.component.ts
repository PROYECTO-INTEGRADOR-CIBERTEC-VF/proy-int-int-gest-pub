import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Apelacion, EstadoApelacion } from '../../models/apelacion.model';
import { ApelacionService } from '../../services/apelacion.service';

@Component({
  selector: 'app-ciudadano-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ciudadano-dashboard.component.html'
})
export class CiudadanoDashboardComponent implements OnInit {
  apelaciones = signal<Apelacion[]>([]);

  apelacionesEnSubsanacion = computed(() => 
    this.apelaciones().filter(a => a.estado === EstadoApelacion.EN_SUBSANACION)
  );

  constructor(private apelacionService: ApelacionService) {}

  ngOnInit() {
    this.cargarApelaciones();
  }

  cargarApelaciones() {
    const usuarioId = 123; 
    this.apelacionService.findByCiudadanoId(usuarioId).subscribe(data => {
      this.apelaciones.set(data);
    });
  }

  
  obtenerDiasRestantes(apelacion: Apelacion): number {
    return apelacion.diasRestantes ?? 0;
  }
}