import { Routes } from '@angular/router';
import { TtaipDashboardComponent } from './pages/ttaip/ttaip-dashboard.component';
import { TtaipSegundaCalificacionComponent } from './pages/ttaip/ttaip-segunda-calificacion.component';

export const routes: Routes = [
  { path: 'ttaip', component: TtaipDashboardComponent },
  { path: 'ttaip/segunda-calificacion/:expediente', component: TtaipSegundaCalificacionComponent },
  { path: '', redirectTo: 'ttaip', pathMatch: 'full' }
];
