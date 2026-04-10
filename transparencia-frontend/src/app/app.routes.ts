import { Routes } from '@angular/router';
import { TtaipDashboardComponent } from './pages/ttaip/ttaip-dashboard.component';
import { TtaipSegundaCalificacionComponent } from './pages/ttaip/ttaip-segunda-calificacion.component';

export const routes: Routes = [
  // Ruta para la bandeja de entrada (FE-01)
  {
    path: 'ttaip',
    component: TtaipDashboardComponent
  },

  // Ruta para el formulario de 2da calificación (FE-02)
  // El ":expediente" permite que la URL lleve el ID del caso (ej. ttaip/segunda-calificacion/00237-2025...)
  {
    path: 'ttaip/segunda-calificacion/:expediente',
    component: TtaipSegundaCalificacionComponent
  },

  // Redirección por defecto si entras a la raíz
  {
    path: '',
    redirectTo: 'ttaip',
    pathMatch: 'full'
  }
];
