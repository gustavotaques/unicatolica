import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cadastro' },
  {
    path: 'cadastro',
    loadComponent: () => import('./cadastro/cadastro').then((m) => m.Cadastro),
  },
];
