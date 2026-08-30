import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Comunidade, ComunidadesService, Pagina } from '../../../core/comunidades/comunidades.service';

const TAMANHO_PAGINA = 12;

/**
 * "Descobrir comunidades" (Story 2.5, RF27/RF28) — lista/filtra todas as comunidades e
 * permite entrar/sair das abertas (Story 2.4). `GET /comunidades` nunca informa
 * `souMembro` (é sempre null na listagem — só o detalhe de uma comunidade preenche),
 * então o estado de "já é membro" é calculado aqui cruzando com `GET /comunidades/minhas`.
 */
@Component({
  selector: 'app-comunidades-lista',
  imports: [RouterLink],
  templateUrl: './comunidades-lista.html',
  styleUrl: './comunidades-lista.scss',
})
export class ComunidadesLista {
  private readonly comunidadesService = inject(ComunidadesService);

  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly pagina = signal<Pagina<Comunidade> | null>(null);
  protected readonly minhasIds = signal<Set<number>>(new Set());
  protected readonly processandoId = signal<number | null>(null);

  protected readonly filtroTipo = signal<'' | 'CURSO' | 'ABERTA'>('');
  protected readonly filtroNome = signal('');
  protected readonly paginaAtual = signal(0);

  constructor() {
    this.carregar();
  }

  /** `tipo` chega como string solta do `<select>` — o template não faz cast de tipo. */
  protected aplicarFiltro(tipo: string, nome: string): void {
    this.filtroTipo.set(tipo === 'CURSO' || tipo === 'ABERTA' ? tipo : '');
    this.filtroNome.set(nome);
    this.paginaAtual.set(0);
    this.carregar();
  }

  protected irParaPagina(numero: number): void {
    this.paginaAtual.set(numero);
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);

    forkJoin({
      minhas: this.comunidadesService.minhas(),
      pagina: this.comunidadesService.listar({
        tipo: this.filtroTipo() || undefined,
        nome: this.filtroNome() || undefined,
        pagina: this.paginaAtual(),
        tamanho: TAMANHO_PAGINA,
      }),
    }).subscribe({
      next: ({ minhas, pagina }) => {
        this.minhasIds.set(new Set(minhas.map((comunidade) => comunidade.id)));
        this.pagina.set(pagina);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar as comunidades agora. Tente novamente em instantes.');
      },
    });
  }

  protected souMembro(comunidade: Comunidade): boolean {
    return this.minhasIds().has(comunidade.id);
  }

  /** Story 2.4 (RF24/RF25) — só comunidades abertas aceitam ingresso por aqui. */
  protected ingressar(comunidade: Comunidade): void {
    if (this.processandoId() !== null) {
      return;
    }

    this.processandoId.set(comunidade.id);
    this.comunidadesService.ingressar(comunidade.id).subscribe({
      next: () => {
        this.processandoId.set(null);
        this.minhasIds.update((ids) => new Set(ids).add(comunidade.id));
      },
      error: () => {
        this.processandoId.set(null);
        this.carregar();
      },
    });
  }

  /** Story 2.4 (RF26). */
  protected sair(comunidade: Comunidade): void {
    if (this.processandoId() !== null) {
      return;
    }

    this.processandoId.set(comunidade.id);
    this.comunidadesService.sair(comunidade.id).subscribe({
      next: () => {
        this.processandoId.set(null);
        this.minhasIds.update((ids) => {
          const novo = new Set(ids);
          novo.delete(comunidade.id);
          return novo;
        });
      },
      error: () => {
        this.processandoId.set(null);
        this.carregar();
      },
    });
  }
}
