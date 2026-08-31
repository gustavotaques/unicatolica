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
    // Casca de navegação global: layout de rota-filha sob um parent `path: ''`.
    // `canActivate` gate um acesso direto ao path do parent; `canActivateChild`
    // gate cada rota autenticada aninhada.
    path: '',
    loadComponent: () => import('./layout/shell/shell').then((m) => m.Shell),
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      {
        path: 'feed',
        loadComponent: () => import('./features/feed/feed').then((m) => m.Feed),
      },
      {
        path: 'comunidades',
        loadComponent: () =>
          import('./features/comunidades/comunidades-lista/comunidades-lista').then((m) => m.ComunidadesLista),
      },
    ],
  },
];
