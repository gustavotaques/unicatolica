import { Injectable, signal } from '@angular/core';

/**
 * Story 14.5 - fixed auto-dismiss duration for every toast.
 *
 * A plain exported TS constant, not a design token: DESIGN.md has no
 * duration token, and this is a behavioural timing value, not a visual one.
 */
export const TOAST_DURACAO_MS = 4000;

/** A single queued toast. `id` is a monotonic in-memory counter, used as the `@for` track key. */
export interface ToastItem {
  readonly id: number;
  readonly mensagem: string;
}

/**
 * Story 14.5 - signal-backed toast queue.
 *
 * `mostrar(mensagem)` appends a non-blank message and schedules its own
 * removal after `TOAST_DURACAO_MS`. There is no public dismiss method: every
 * toast self-removes on its own timer, never on user action
 * (EXPERIENCE.md: "some sozinho").
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  #proximoId = 0;
  readonly #itens = signal<ToastItem[]>([]);
  readonly toasts = this.#itens.asReadonly();

  mostrar(mensagem: string): void {
    const texto = (mensagem ?? '').trim();
    if (!texto) {
      return;
    }

    const id = this.#proximoId++;
    this.#itens.update((itens) => [...itens, { id, mensagem: texto }]);

    setTimeout(() => {
      this.#itens.update((itens) => itens.filter((item) => item.id !== id));
    }, TOAST_DURACAO_MS);
  }
}
