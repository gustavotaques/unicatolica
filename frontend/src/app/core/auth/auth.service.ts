import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
}

const TOKEN_STORAGE_KEY = 'pacext.token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly http: HttpClient) {}

  login(email: string, senha: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/auth/login`, { email, senha } satisfies LoginRequest)
      .pipe(tap((resposta) => this.armazenarToken(resposta.token)));
  }

  private armazenarToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }

  obterToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  /**
   * Header `Authorization` pronto pra passar em `{ headers }` de qualquer chamada
   * autenticada — ainda não existe um `HttpInterceptor` global (AD-7 não decidiu isso
   * ainda), então cada serviço que fala com endpoint autenticado usa isto explicitamente.
   */
  obterCabecalhoAutorizacao(): Record<string, string> {
    const token = this.obterToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  logout(): void {
    const headers = this.obterCabecalhoAutorizacao();
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    if (headers['Authorization']) {
      // Encerra a sessão no servidor (Story 1.6) — best-effort: mesmo se falhar (ex.: token
      // já expirado), a sessão local já foi encerrada acima, que é o que importa para o usuário.
      this.http.post(`${API_BASE_URL}/auth/logout`, null, { headers }).subscribe({ error: () => undefined });
    }
  }
}
