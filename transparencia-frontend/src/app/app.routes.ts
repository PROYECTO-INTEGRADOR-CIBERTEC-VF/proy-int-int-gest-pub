import { Routes } from '@angular/router';
import { TtaipDashboardComponent } from './pages/ttaip/ttaip-dashboard.component';

export const routes: Routes = [
  { path: 'ttaip', component: TtaipDashboardComponent },
  { path: '', redirectTo: 'ttaip', pathMatch: 'full' }
];
