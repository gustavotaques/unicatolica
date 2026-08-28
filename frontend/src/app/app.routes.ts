import { Routes } from '@angular/router';

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
];
