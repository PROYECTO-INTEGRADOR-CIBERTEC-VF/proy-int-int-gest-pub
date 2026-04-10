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
  filtroActual: string = 'TODOS';

  expedientes = [
    { id: '00237-2025-JUS/TTAIP', ciudadano: 'Juan Perez', fecha: '10/04/2026', estado: 'SUBSANADO' },
    { id: '00238-2025-JUS/TTAIP', ciudadano: 'Maria Lopez', fecha: '09/04/2026', estado: 'PENDIENTE' }
  ];

  get expedientesFiltrados() {
    if (this.filtroActual === 'TODOS') return this.expedientes;
    return this.expedientes.filter(e => e.estado === 'SUBSANADO');
  }

  setFiltro(nuevoFiltro: string) {
    this.filtroActual = nuevoFiltro;
  }
}
