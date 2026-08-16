import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  template: `
    <main class="shell">
      <header class="brand">
        <h1>UniCatólica</h1>
        <p>Rede Social Acadêmica — Centro Universitário Católica de Santa Catarina</p>
      </header>

      <section class="card">
        <h2>Bem-vindo(a)</h2>
        <p>
          Conecte-se com estudantes de qualquer curso, participe de
          comunidades, compartilhe materiais e tire dúvidas com veteranos.
        </p>
        <p class="status">Tela inicial — estrutura de identificação do projeto (checkpoint PAC VI).</p>
      </section>
    </main>
  `,
  styles: [`
    .shell {
      max-width: 640px;
      margin: 4rem auto;
      padding: 0 1.5rem;
    }
    .brand h1 {
      margin: 0;
      font-size: 2.25rem;
      color: #8B1A1A;
    }
    .brand p {
      margin: 0.25rem 0 2rem;
      color: #555;
    }
    .card {
      background: #fff;
      border: 1px solid #e2e0dc;
      border-radius: 12px;
      padding: 1.5rem 1.75rem;
    }
    .status {
      font-size: 0.85rem;
      color: #888;
      font-style: italic;
    }
  `],
})
export class AppComponent {
  title = 'UniCatólica';
}
