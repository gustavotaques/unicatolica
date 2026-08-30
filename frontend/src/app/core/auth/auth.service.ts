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

  logout(): void {
    const token = this.obterToken();
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    if (token) {
      // Encerra a sessão no servidor (Story 1.6) — best-effort: mesmo se falhar (ex.: token
      // já expirado), a sessão local já foi encerrada acima, que é o que importa para o usuário.
      this.http
        .post(`${API_BASE_URL}/auth/logout`, null, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .subscribe({ error: () => undefined });
    }
  }
}
