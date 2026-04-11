import { Routes } from '@angular/router';
import { TtaipSegundaCalificacionComponent } from './pages/ttaip/ttaip-segunda-calificacion.component';

export const routes: Routes = [
  // Solo dejamos la ruta de la segunda calificación en esta rama
  { path: 'ttaip/segunda-calificacion/:expediente', component: TtaipSegundaCalificacionComponent }
];
