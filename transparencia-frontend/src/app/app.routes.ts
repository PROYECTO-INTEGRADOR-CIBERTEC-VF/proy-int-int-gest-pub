import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: '',
		pathMatch: 'full',
		redirectTo: 'login'
	},
	{
		path: 'login',
		loadComponent: () => import('./pages/auth/login.component').then((m) => m.LoginComponent),
		data: { accessMode: 'citizen' }
	},
	{
		path: 'acceso-funcionario',
		loadComponent: () => import('./pages/auth/login.component').then((m) => m.LoginComponent),
		data: { accessMode: 'internal', requiredRole: 'FUNCIONARIO' }
	},
	{
		path: 'acceso-ttaip',
		loadComponent: () => import('./pages/auth/login.component').then((m) => m.LoginComponent),
		data: { accessMode: 'internal', requiredRole: 'TTAIP' }
	},
	{
		path: 'acceso-admin',
		loadComponent: () => import('./pages/auth/login.component').then((m) => m.LoginComponent),
		data: { accessMode: 'internal', requiredRole: 'ADMINISTRADOR' }
	},
	{
		path: 'registro',
		loadComponent: () => import('./pages/auth/registro.component').then((m) => m.RegistroComponent)
	},
	{
		path: '**',
		redirectTo: 'login'
	}
];
