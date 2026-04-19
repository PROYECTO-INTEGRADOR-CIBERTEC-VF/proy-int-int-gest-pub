import { Routes } from '@angular/router';
export const routes: Routes = [
  {
    path: 'ciudadano/nueva-saip',
    loadComponent: () => import('./pages/ciudadano/nueva-saip.component').then(m => m.NuevaSaipComponent)
  },
  {
    path: 'ttaip',
    loadComponent: () => import('./pages/ttaip/ttaip-dashboard.component').then(m => m.TtaipDashboardComponent)
  },
  {
    path: 'ttaip/segunda-calificacion',
    loadComponent: () => import('./pages/ttaip/ttaip-segunda-calificacion.component').then(m => m.TtaipSegundaCalificacionComponent)
  },
  {
    path: 'ttaip/segunda-calificacion/:expediente',
    loadComponent: () => import('./pages/ttaip/ttaip-segunda-calificacion.component').then(m => m.TtaipSegundaCalificacionComponent)
  },
  {
    path: 'ttaip/resolver/:expediente',
    loadComponent: () => import('./pages/ttaip/ttaip-resolver.component').then(m => m.TtaipResolverComponent)
  },
  { path: '', redirectTo: 'ttaip', pathMatch: 'full' },
  { path: '**', redirectTo: 'ttaip' }
];
