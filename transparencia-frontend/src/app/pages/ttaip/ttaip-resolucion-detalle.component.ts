import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-ttaip-resolucion-detalle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ttaip-resolucion-detalle.component.html'
})
export class TtaipResolucionDetalleComponent implements OnInit {
  expediente: string = '';
  activeTab: string = 'apelacion'; // Controla las pestañas

  // Flag para mostrar el banner de proceso disciplinario (Requerimiento del Backlog)
  aplicaProcesoDisciplinario: boolean = true;

  // Línea de tiempo con las 3 resoluciones
  timeline = [
    {
      title: '1ra Calificación - ADMISIBLE',
      datetime: '08/09/2025 10:15',
      description: 'La apelación cumple con los requisitos formales. Se procedió con el análisis de fondo.',
      status: 'completed'
    },
    {
      title: '2da Calificación - EVALUACIÓN',
      datetime: '15/09/2025 11:30',
      description: 'Revisión de descargos de la entidad y evaluación de la materia de transparencia.',
      status: 'completed'
    },
    {
      title: 'Resolución Final - FUNDADO',
      datetime: '20/09/2025 16:45',
      description: 'Se acepta la apelación. Se ordena a la entidad entregar la información en 10 días hábiles.',
      status: 'current'
    }
  ];

  attachments = [
    { name: 'SAIP_Original.pdf', meta: 'PDF • 345 KB' },
    { name: 'Respuesta_Entidad.pdf', meta: 'PDF • 512 KB' },
    { name: 'Recurso_Fundamentado.pdf', meta: 'PDF • 876 KB' },
    { name: 'Resolucion_Final.pdf', meta: 'PDF • 1.2 MB' }
  ];

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    // Obtenemos el expediente de la URL
    this.expediente = this.route.snapshot.paramMap.get('expediente') || '00234-2025-JUS/TTAIP';
  }

  volver(): void {
    // Regresa al dashboard (puedes ajustarlo si el botón está en el dashboard del ciudadano)
    this.router.navigate(['/ttaip']);
  }
}
