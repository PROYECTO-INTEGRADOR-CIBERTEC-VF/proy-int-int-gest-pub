import { Routes } from '@angular/router';
import { tipoUsuarioGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/auth/login.component').then(m => m.LoginComponent),
    data: { accessMode: 'citizen' }
  },
  {
    path: 'acceso-funcionario',
    loadComponent: () => import('./pages/auth/login.component').then(m => m.LoginComponent),
    data: { accessMode: 'internal', requiredRole: 'FUNCIONARIO' }
  },
  {
    path: 'acceso-ttaip',
    loadComponent: () => import('./pages/auth/login.component').then(m => m.LoginComponent),
    data: { accessMode: 'internal', requiredRole: 'TTAIP' }
  },
  {
    path: 'acceso-admin',
    loadComponent: () => import('./pages/auth/login.component').then(m => m.LoginComponent),
    data: { accessMode: 'internal', requiredRole: 'ADMINISTRADOR' }
  },
  {
    path: 'registro',
    loadComponent: () => import('./pages/auth/registro.component').then(m => m.RegistroComponent)
  },
  {
    path: 'ciudadano',
    redirectTo: 'ciudadano/nueva-saip',
    pathMatch: 'full',
    canActivate: [tipoUsuarioGuard('CIUDADANO')]
  },
  {
    path: 'ciudadano/nueva-saip',
    loadComponent: () => import('./pages/ciudadano/nueva-saip.component').then(m => m.NuevaSaipComponent),
    canActivate: [tipoUsuarioGuard('CIUDADANO')]
  },
  {
    path: 'ciudadano/nueva-apelacion',
    loadComponent: () => import('./pages/ciudadano/nueva-apelacion.component').then(m => m.NuevaApelacionComponent),
    canActivate: [tipoUsuarioGuard('CIUDADANO')]
  },
  {
    path: 'ciudadano/subsanacion',
    loadComponent: () => import('./pages/ciudadano/subsanacion.component').then(m => m.SubsanacionComponent),
    canActivate: [tipoUsuarioGuard('CIUDADANO')]
  },
  {
    path: 'funcionario',
    loadComponent: () => import('./pages/funcionario/funcionario-dashboard.component').then(m => m.FuncionarioDashboardComponent),
    canActivate: [tipoUsuarioGuard('FUNCIONARIO')]
  },
  {
    path: 'funcionario/solicitud/:expediente',
    loadComponent: () => import('./pages/funcionario/funcionario-solicitud-detalle.component').then(m => m.FuncionarioSolicitudDetalleComponent),
    canActivate: [tipoUsuarioGuard('FUNCIONARIO')]
  },
  {
    path: 'funcionario/responder/:expediente',
    loadComponent: () => import('./pages/funcionario/funcionario-responder.component').then(m => m.FuncionarioResponderComponent),
    canActivate: [tipoUsuarioGuard('FUNCIONARIO')]
  },
  {
    path: 'ttaip',
    loadComponent: () => import('./pages/ttaip/ttaip-dashboard.component').then(m => m.TtaipDashboardComponent),
    canActivate: [tipoUsuarioGuard('TTAIP')]
  },
  {
    path: 'ttaip/segunda-calificacion',
    loadComponent: () => import('./pages/ttaip/ttaip-segunda-calificacion.component').then(m => m.TtaipSegundaCalificacionComponent),
    canActivate: [tipoUsuarioGuard('TTAIP')]
  },
  {
    path: 'ttaip/segunda-calificacion/:expediente',
    loadComponent: () => import('./pages/ttaip/ttaip-segunda-calificacion.component').then(m => m.TtaipSegundaCalificacionComponent),
    canActivate: [tipoUsuarioGuard('TTAIP')]
  },
  {
    path: 'ttaip/resolver/:expediente',
    loadComponent: () => import('./pages/ttaip/ttaip-resolver.component').then(m => m.TtaipResolverComponent),
    canActivate: [tipoUsuarioGuard('TTAIP')]
  },
  { path: '**', redirectTo: 'login' }
];
