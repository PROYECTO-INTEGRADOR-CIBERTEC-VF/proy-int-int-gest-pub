import { Routes } from '@angular/router';
export const routes: Routes = [
  {
    path: 'ciudadano',
    redirectTo: 'ciudadano/nueva-saip',
    pathMatch: 'full'
  },
  {
    path: 'ciudadano/nueva-saip',
    loadComponent: () => import('./pages/ciudadano/nueva-saip.component').then(m => m.NuevaSaipComponent)
  },
  {
    path: 'ciudadano/nueva-apelacion',
    loadComponent: () => import('./pages/ciudadano/nueva-apelacion.component').then(m => m.NuevaApelacionComponent)
  },
  {
    path: 'ciudadano/subsanacion',
    loadComponent: () => import('./pages/ciudadano/subsanacion.component').then(m => m.SubsanacionComponent)
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
  { path: '', redirectTo: 'ttaip', pathMatch: 'full' },
  { path: '**', redirectTo: 'ttaip' }
];
