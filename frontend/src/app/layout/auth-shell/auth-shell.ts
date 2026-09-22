import { Component } from '@angular/core';

/**
 * Story 14.7 - casca das telas públicas (Login, Cadastro, Verifique seu e-mail).
 *
 * Dá a essas três telas o mesmo enquadramento: o landmark `<main>`, a marca
 * (traço bordô + "UniCatólica", mesmo idioma do `shell.html` autenticado) e um
 * card centralizado em `surface`. Cada tela projeta o próprio `<h1>` e o
 * próprio conteúdo, então o heading continua pertencendo à tela (e continua
 * sendo o que os testes e2e consultam).
 *
 * Sem `@Input` / `@Output`: a casca não tem variação. O card usa borda de 1px e
 * nunca sombra, como todo card do sistema (DESIGN.md "Elevation & Depth").
 *
 * Atenção ao encapsulamento: o conteúdo projetado carrega o atributo da tela
 * consumidora, não o desta casca, então regras daqui não alcançam o `<h1>` ou
 * os campos - cada tela estiliza o que está no próprio template.
 */
@Component({
  selector: 'uc-auth-shell',
  templateUrl: './auth-shell.html',
  styleUrl: './auth-shell.scss',
})
export class UcAuthShell {}
