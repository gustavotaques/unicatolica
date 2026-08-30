import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/identidade/login/login').then((m) => m.Login),
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./cadastro/cadastro').then((m) => m.Cadastro),
  },
  {
    path: 'confirmar-email',
    loadComponent: () => import('./confirmar-email/confirmar-email').then((m) => m.ConfirmarEmail),
  },
  {
    path: 'feed',
    canActivate: [authGuard],
    loadComponent: () => import('./features/feed/feed').then((m) => m.Feed),
  },
  {
    path: 'comunidades',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/comunidades/comunidades-lista/comunidades-lista').then((m) => m.ComunidadesLista),
  },
];
