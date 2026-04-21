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
    { expediente: 'EXP-2026-001', ciudadano: 'Carlos Ruiz', fecha: '2026-04-10', estado: 'EN_CALIFICACION_2', diasHabiles: '5 días' },
    { expediente: 'EXP-2026-002', ciudadano: 'Ana Torres', fecha: '2026-04-09', estado: 'EN_CALIFICACION_1', diasHabiles: '4 días' },
    { expediente: 'EXP-2026-003', ciudadano: 'Luis Mendoza', fecha: '2026-04-10', estado: 'EN_CALIFICACION_2', diasHabiles: '5 días' },
    { expediente: 'EXP-2026-004', ciudadano: 'Juan Marquez', fecha: '2026-04-11', estado: 'EN_RESOLUCION', diasHabiles: '6 días' },
    { expediente: '00234-2025-JUS/TTAIP', ciudadano: 'Juan Carlos Perez', fecha: '2026-04-05', estado: 'RESUELTO', diasHabiles: 'Completado' }
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
