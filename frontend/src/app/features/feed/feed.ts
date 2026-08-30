import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Comunidade, ComunidadesService } from '../../core/comunidades/comunidades.service';
import { Usuario, UsuarioService } from '../../core/usuario/usuario.service';

/**
 * Home — modelo visual "Telas-Chave UniCatólica" (direção Campus Clean, ver
 * docs/modelo-epico-2-comunidades.md e a home-comunidade.html do bmad). Só mostra o que
 * já é real: saudação (RF12), "Suas comunidades" e descoberta de comunidades abertas
 * (Epic 2). O feed de publicações do mockup é fabricado (Publicações é outro Épico,
 * ainda sem backend) — por isso vira um aviso "em breve" em vez de posts inventados.
 * Tokens de cor do Campus Clean ficam locais deste componente (feed.scss): Epic 14
 * (Design System compartilhado) ainda não existe.
 */
@Component({
  selector: 'app-feed',
  imports: [RouterLink],
  templateUrl: './feed.html',
  styleUrl: './feed.scss',
})
export class Feed {
  private readonly authService = inject(AuthService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly comunidadesService = inject(ComunidadesService);
  private readonly router = inject(Router);

  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);
  protected readonly usuario = signal<Usuario | null>(null);
  protected readonly minhasComunidades = signal<Comunidade[]>([]);
  protected readonly descobrir = signal<Comunidade[]>([]);
  protected readonly ingressandoId = signal<number | null>(null);

  protected readonly iniciais = computed(() => {
    const nome = this.usuario()?.nome ?? '';
    return nome
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((parte) => parte[0]?.toUpperCase() ?? '')
      .join('');
  });

  constructor() {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);

    forkJoin({
      usuario: this.usuarioService.me(),
      minhas: this.comunidadesService.minhas(),
      abertas: this.comunidadesService.listar({ tipo: 'ABERTA', tamanho: 6 }),
    }).subscribe({
      next: ({ usuario, minhas, abertas }) => {
        const idsQueJaSouMembro = new Set(minhas.map((comunidade) => comunidade.id));
        this.usuario.set(usuario);
        this.minhasComunidades.set(minhas);
        this.descobrir.set(abertas.content.filter((comunidade) => !idsQueJaSouMembro.has(comunidade.id)));
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar sua Home agora. Tente novamente em instantes.');
      },
    });
  }

  /** Story 2.4 (RF24) — ingressa direto da Home, sem passar pela lista completa. */
  protected ingressar(comunidade: Comunidade): void {
    if (this.ingressandoId() !== null) {
      return;
    }

    this.ingressandoId.set(comunidade.id);
    this.comunidadesService.ingressar(comunidade.id).subscribe({
      next: () => {
        this.ingressandoId.set(null);
        this.descobrir.update((lista) => lista.filter((item) => item.id !== comunidade.id));
        this.minhasComunidades.update((lista) => [...lista, { ...comunidade, souMembro: true }]);
      },
      error: () => {
        // Provável corrida rara (já é membro) — recarrega pra refletir o estado real
        // em vez de deixar o botão "preso" em "Entrando…".
        this.ingressandoId.set(null);
        this.carregar();
      },
    });
  }

  protected sair(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
