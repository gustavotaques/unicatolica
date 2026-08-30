import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { API_BASE_URL } from '../config/api.config';

/** Espelha `ComunidadeResponse` do backend (Stories 2.2/2.4/2.5). */
export interface Comunidade {
  id: number;
  nome: string;
  descricao: string | null;
  tipo: 'CURSO' | 'ABERTA';
  souMembro: boolean | null;
  criadoEm: string;
}

/** Espelha `PageResponse<T>` do backend (AD-4). */
export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/**
 * Fala com o módulo Comunidades do Epic 2 (protótipo, ver
 * docs/modelo-epico-2-comunidades.md) — listar/filtrar, "minhas comunidades" da Home,
 * entrar/sair. Criação de comunidade aberta (Story 2.2) fica pra quando a tela de
 * criação existir.
 */
@Injectable({ providedIn: 'root' })
export class ComunidadesService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  /** `GET /comunidades/minhas` — "Suas comunidades" na barra lateral da Home. */
  minhas(): Observable<Comunidade[]> {
    return this.http.get<Comunidade[]>(`${API_BASE_URL}/comunidades/minhas`, {
      headers: this.authService.obterCabecalhoAutorizacao(),
    });
  }

  /** `GET /comunidades` (Story 2.5, RF27/RF28) — lista/filtra, paginado. */
  listar(opcoes: { tipo?: 'CURSO' | 'ABERTA'; nome?: string; pagina?: number; tamanho?: number } = {}): Observable<
    Pagina<Comunidade>
  > {
    const parametros: Record<string, string> = {};
    if (opcoes.tipo) parametros['tipo'] = opcoes.tipo;
    if (opcoes.nome) parametros['nome'] = opcoes.nome;
    parametros['pagina'] = String(opcoes.pagina ?? 0);
    parametros['tamanho'] = String(opcoes.tamanho ?? 20);

    return this.http.get<Pagina<Comunidade>>(`${API_BASE_URL}/comunidades`, {
      headers: this.authService.obterCabecalhoAutorizacao(),
      params: parametros,
    });
  }

  /** `POST /comunidades/{id}/membros` (Story 2.4, RF24/RF25) — só comunidades abertas. */
  ingressar(id: number): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/comunidades/${id}/membros`, null, {
      headers: this.authService.obterCabecalhoAutorizacao(),
    });
  }

  /** `DELETE /comunidades/{id}/membros/me` (Story 2.4, RF26). */
  sair(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/comunidades/${id}/membros/me`, {
      headers: this.authService.obterCabecalhoAutorizacao(),
    });
  }
}
