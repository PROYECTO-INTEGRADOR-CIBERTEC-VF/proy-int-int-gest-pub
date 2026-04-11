import { Routes } from '@angular/router';
import { TtaipDashboardComponent } from './pages/ttaip/ttaip-dashboard.component';

export const routes: Routes = [
  // Hacemos que el Dashboard cargue directamente al entrar a la página
  { path: '', component: TtaipDashboardComponent }
];
