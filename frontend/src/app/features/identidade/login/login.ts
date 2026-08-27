import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  protected readonly form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });

  protected readonly enviando = signal(false);
  protected readonly mensagemErro = signal<string | null>(null);
  protected readonly logado = signal(false);

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, senha } = this.form.getRawValue();
    this.enviando.set(true);
    this.mensagemErro.set(null);

    this.authService.login(email!, senha!).subscribe({
      next: () => {
        this.enviando.set(false);
        this.logado.set(true);
      },
      error: () => {
        this.enviando.set(false);
        this.mensagemErro.set('E-mail ou senha inválidos.');
      },
    });
  }
}
