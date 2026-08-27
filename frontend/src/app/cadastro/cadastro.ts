import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

/**
 * Envelope de erro padrão da API (AD-5) — espelha `ErroResponse` do backend.
 */
interface ErroResponse {
  error: {
    code: string;
    message: string;
    details: string | null;
  };
}

interface CadastroResponse {
  id: number;
  nome: string;
  email: string;
  curso: string;
  emailConfirmado: boolean;
  criadoEm: string;
}

/**
 * Tela de cadastro (Story 1.2) — funcional, sem o Design System "Campus Clean" (Epic 14,
 * ainda não implementado). Cobre os critérios de aceite da história: envia nome, e-mail
 * institucional, senha, curso e data de nascimento para `POST /auth/registro`, e exibe as
 * mensagens de rejeição específicas por cenário (e-mail duplicado, domínio externo,
 * validação de campo, idade mínima) — nunca uma mensagem genérica de erro.
 */
@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.scss',
})
export class Cadastro {
  private readonly http = inject(HttpClient);
  private readonly formBuilder = inject(FormBuilder);

  /**
   * [DECISÃO A CONFIRMAR] URL da API hardcoded — o frontend ainda não lê configuração de
   * ambiente (ver comentário de `API_URL` em `.env.example`); ajustar quando essa leitura
   * existir.
   */
  private readonly apiUrl = 'http://localhost:8080';

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<CadastroResponse | null>(null);

  protected readonly form = this.formBuilder.nonNullable.group({
    nome: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
    curso: ['', [Validators.required]],
    dataNascimento: ['', [Validators.required]],
  });

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);
    this.sucesso.set(null);

    this.http.post<CadastroResponse>(`${this.apiUrl}/auth/registro`, this.form.getRawValue()).subscribe({
      next: (resposta) => {
        this.enviando.set(false);
        this.sucesso.set(resposta);
        this.form.reset();
      },
      error: (resposta: HttpErrorResponse) => {
        this.enviando.set(false);
        this.erro.set(this.mensagemDeErro(resposta));
      },
    });
  }

  private mensagemDeErro(resposta: HttpErrorResponse): string {
    const corpo = resposta.error as ErroResponse | null;
    if (corpo?.error?.message) {
      return corpo.error.message;
    }
    return 'Não foi possível completar o cadastro. Tente novamente em instantes.';
  }
}
