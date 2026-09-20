import { isDevMode } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { ToastService } from './app/ui/toast/toast.service';

bootstrapApplication(App, appConfig)
  .then((appRef) => {
    // Ponto de teste E2E para os toasts (Story 14.5): expõe `mostrar()` em
    // `window.__ucToast` SÓ em dev mode (`ng serve`). As Stories 2.3/2.4 já
    // ligaram gatilhos reais (join de comunidade, auto-join na Home), mas
    // esses fluxos são de Comunidades (Epic 2), fora do escopo do E2E de
    // toast/motion daqui - este seam isola o primitivo `ToastService` sem
    // depender de mockar a lista de comunidades. Some da build de produção
    // (guarda `isDevMode()`).
    if (isDevMode()) {
      (globalThis as unknown as { __ucToast?: ToastService }).__ucToast =
        appRef.injector.get(ToastService);
    }
  })
  .catch((err) => console.error(err));
