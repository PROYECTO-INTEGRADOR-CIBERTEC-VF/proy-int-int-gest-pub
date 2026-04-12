import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-ttaip-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './ttaip-dashboard.component.html',
  styleUrl: './ttaip-dashboard.component.css'
})
export class TtaipDashboardComponent {
  // Datos simulados temporales para maquetación y pruebas
  apelaciones = [
    { expediente: 'EXP-2026-001', estado: 'EN_CALIFICACION_2', ciudadano: 'Carlos Ruiz', fecha: '2026-04-10' },
    { expediente: 'EXP-2026-002', estado: 'EN_CALIFICACION_1', ciudadano: 'Ana Torres', fecha: '2026-04-09' },
    { expediente: 'EXP-2026-003', estado: 'EN_CALIFICACION_2', ciudadano: 'Luis Mendoza', fecha: '2026-04-10' },
    { expediente: 'EXP-2026-004',estado: 'EN_RESOLUCION',ciudadano: 'Juan Marquez',fecha: '2026-04-11' }
  ];

  filtroActual: string = 'TODOS';

  get apelacionesFiltradas() {
    if (this.filtroActual === 'SUBSANADAS') {
      return this.apelaciones.filter(a => a.estado === 'EN_CALIFICACION_2');
    }
    return this.apelaciones;
  }

  setFiltro(filtro: string) {
    this.filtroActual = filtro;
  }
}
