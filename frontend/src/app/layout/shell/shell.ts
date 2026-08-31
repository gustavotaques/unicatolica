import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/** Um item da navegação global. `path === null` = item ainda sem rota (inerte). */
interface NavItem {
  label: string;
  path: string | null;
  /** Só aparece para MODERADOR / ADMINISTRADOR. */
  privileged?: boolean;
}

/**
 * Itens da sidebar, na ordem exata da tabela "Navegação global" de
 * EXPERIENCE.md: Denúncias e Solicitações de fixação ficam entre "Criar
 * enquete" e "Suas comunidades", e só aparecem para MODERADOR / ADMINISTRADOR.
 * Hoje só "Início" tem rota - o resto entra como rota conforme cada epic
 * aterrissa.
 */
const NAV_ITENS: readonly NavItem[] = [
  { label: 'Início', path: '/feed' },
  { label: 'Buscar', path: null },
  { label: 'Mensagens', path: null },
  { label: 'Notificações', path: null },
  { label: 'Criar enquete', path: null },
  { label: 'Denúncias', path: null, privileged: true },
  { label: 'Solicitações de fixação', path: null, privileged: true },
  { label: 'Suas comunidades', path: null },
  { label: 'Descobrir comunidades', path: null },
];

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
  host: {
    '(document:keydown.escape)': 'fecharMenu()',
    '(document:click)': 'fecharMenu()',
  },
})
export class Shell {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  private readonly avatar = viewChild<ElementRef<HTMLButtonElement>>('avatar');

  protected readonly navItens = NAV_ITENS;
  protected readonly menuAberto = signal(false);

  /**
   * Derivado uma única vez (não é um método re-executado por item do `@for`).
   * `possuiPerfil` não é signal, então o `computed` sem dependências avalia na
   * primeira leitura e memoiza - que é exatamente o que queremos aqui.
   */
  protected readonly ehPrivilegiado = computed(
    () => this.auth.possuiPerfil('MODERADOR') || this.auth.possuiPerfil('ADMINISTRADOR'),
  );

  protected alternarMenu(evento: Event): void {
    // Impede que o clique borbulhe até o listener `document:click` e feche o
    // menu no mesmo gesto que o abriu.
    evento.stopPropagation();
    this.menuAberto.update((aberto) => !aberto);
  }

  /**
   * Fecha o dropdown e devolve o foco ao avatar. Caminho de Escape, clique
   * fora e ativação de item - onde o foco precisa voltar para um lugar
   * previsível.
   */
  protected fecharMenu(): void {
    if (!this.menuAberto()) {
      return;
    }
    this.menuAberto.set(false);
    this.avatar()?.nativeElement.focus();
  }

  /**
   * Fecha o dropdown quando o foco sai dele (ex.: Tab a partir de "Sair"). Não
   * devolve o foco ao avatar: o foco já está indo para outro lugar por vontade
   * do usuário.
   */
  protected aoSairFoco(evento: FocusEvent): void {
    const menu = evento.currentTarget as HTMLElement;
    const proximo = evento.relatedTarget as Node | null;
    if (proximo && menu.contains(proximo)) {
      return;
    }
    this.menuAberto.set(false);
  }

  protected sair(): void {
    this.fecharMenu();
    this.auth.logout();
    this.router.navigateByUrl('/login').catch(() => {});
  }
}
