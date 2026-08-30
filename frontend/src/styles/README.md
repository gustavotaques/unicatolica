# Camada de tokens de design (Story 14.1)

Esta pasta e a **fonte unica** de valores de design do frontend: paleta, escala
tipografica, espacamento, raio e a sombra de overlay. Tudo vem, verbatim, do
`DESIGN.md` do UX ("Campus Clean"):
`_bmad-output/planning-artifacts/ux-designs/ux-unicatolica-2026-08-17/DESIGN.md`.

## Arquivos

| Arquivo            | Papel                                                                                                             |
| ------------------ | ----------------------------------------------------------------------------------------------------------------- |
| `_tokens.scss`     | Bloco `:root` unico. Todas as custom properties `--uc-*`. Fonte da verdade.                                       |
| `_typography.scss` | Classes utilitarias `.uc-text-*`, uma por papel de tipografia. Compoem so `var(--uc-*)`.                          |
| `../styles.scss`   | Unico entry point de build. Agrega os parciais via `@use`.                                                        |
| `tokens.spec.ts`   | Compila `styles.scss` com `sass` e trava os tokens, a tabela deste README e as classes `.uc-text-*` contra drift. |

## Regra: consuma via `var(--uc-*)`, nunca hardcode

Componentes e telas em `frontend/src/app/**` **nunca** escrevem um hex, px, peso
de fonte ou raio literal. Eles leem o token:

```scss
.card {
  background: var(--uc-color-surface);
  border: 1px solid var(--uc-color-border);
  border-radius: var(--uc-radius-md);
  padding: var(--uc-space-card-padding);
}
```

```html
<h1 class="uc-text-greeting">Ola, Julia</h1>
<p class="uc-text-body">Corpo de postagem, texto corrido.</p>
```

### Valor ausente do `DESIGN.md` = "Ask First"

Se uma story consumidora (14.2, 14.7, ...) precisa de um valor que o `DESIGN.md`
nao define (focus ring, scrim de overlay, estado desabilitado), isso e uma
escalada **"Ask First"** na story consumidora, nao um literal silencioso. Nenhum
token novo entra aqui sem passar pelo `DESIGN.md`.

## `.uc-text-*` vs classe de componente

- Use `.uc-text-*` para um papel de tipografia puro em um template
  (`<span class="uc-text-meta">ha 2 h</span>`).
- A Story 14.2 introduz classes de componente (badge, botao, card) que compoem
  os mesmos tokens. Para um componente, use a classe de componente, nao a
  utilitaria de tipografia.
- A Story 14.7 migra as telas ja existentes (Login, Cadastro, Verifique seu
  e-mail) do SCSS por componente com valores hardcoded para consumo de token.

## Modo escuro

Fora de escopo do MVP (decisao confirmada no `DESIGN.md`). Nao ha bloco
`@media (prefers-color-scheme)` nem token dark. Nao adicione um sem renegociar.

## Nota sobre a pilha de fontes

O `DESIGN.md` marca a pilha de fontes de sistema como **pendencia do time**:
nenhuma tipografia de marca (Google Font ou similar) foi avaliada. A pilha atual
e uma escolha pragmatica; uma fonte de exibicao propria mais adiante e uma
revisao explicita do `DESIGN.md`, nao um bloqueio para esta camada.

As classes `.uc-text-*` **nao** aplicam `font-family` (nenhum papel do
`DESIGN.md` define uma). A pilha de sistema so passa a valer quando a Story 14.7
a aplicar uma vez no documento; ate la, texto marcado com `.uc-text-*` herda a
fonte default do navegador. Isso e intencional para manter a 14.1 sem impacto
visual.

## Tabela de tokens

Nome -> valor -> papel no `DESIGN.md`. Esta tabela e verificada contra
`_tokens.scss` compilado por `tokens.spec.ts` (linha fora de sincronia = teste
vermelho).

### Paleta (`colors`)

| Token                    | Valor     | Papel                                                                                                                    |
| ------------------------ | --------- | ------------------------------------------------------------------------------------------------------------------------ |
| `--uc-color-bg`          | `#FAFAF8` | `colors.bg` - canvas base, quase branco                                                                                  |
| `--uc-color-surface`     | `#FFFFFF` | `colors.surface` - superficie de card                                                                                    |
| `--uc-color-border`      | `#EAEAE6` | `colors.border` - divisoria / contorno de card (1px)                                                                     |
| `--uc-color-ink`         | `#1C1C1A` | `colors.ink` - texto de conteudo principal                                                                               |
| `--uc-color-ink-soft`    | `#6B6B66` | `colors.ink-soft` - texto secundario (metadados, timestamps)                                                             |
| `--uc-color-ink-faint`   | `#A2A29C` | `colors.ink-faint` - texto terciario (labels, placeholders)                                                              |
| `--uc-color-maroon`      | `#7A1F2B` | `colors.maroon` - acento institucional minimo (traco, icone ativo, titulo de comunidade de curso); nunca fundo dominante |
| `--uc-color-orange`      | `#EA6A2E` | `colors.orange` - unico acento de acao forte (CTA primario, tag de destaque)                                             |
| `--uc-color-orange-tint` | `#FDEEE6` | `colors.orange-tint` - fundo suave de badge / nav ativa; nunca texto                                                     |
| `--uc-color-green-ok`    | `#3A7D5C` | `colors.green-ok` - unico token de sucesso (membro, confirmacao positiva)                                                |

### Tipografia (`typography`)

| Token                            | Valor                                                                         | Papel                                                                     |
| -------------------------------- | ----------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `--uc-font-family-base`          | `-apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif` | pilha de sistema, todos os papeis (pendencia do time - ver acima)         |
| `--uc-font-size-greeting`        | `22px`                                                                        | `typography.greeting` - saudacao no topo do Inicio                        |
| `--uc-font-size-question`        | `15px`                                                                        | `typography.question` - perguntas de enquete, titulos de card             |
| `--uc-font-size-body`            | `13.5px`                                                                      | `typography.body` - corpo de postagem, texto corrido                      |
| `--uc-font-size-meta`            | `12px`                                                                        | `typography.meta` - timestamps, contagens, texto secundario               |
| `--uc-font-size-label-caps`      | `10.5px`                                                                      | `typography.label-caps` - labels em caixa alta, nomes de secao            |
| `--uc-font-weight-regular`       | `400`                                                                         | peso regular (`body`, `meta`)                                             |
| `--uc-font-weight-semibold`      | `600`                                                                         | peso de enfase (`greeting`, `question`)                                   |
| `--uc-font-weight-caps`          | `700`                                                                         | peso reservado a `label-caps`                                             |
| `--uc-line-height-body`          | `1.5`                                                                         | `typography.body.lineHeight` (unico papel com line-height no `DESIGN.md`) |
| `--uc-letter-spacing-greeting`   | `-0.01em`                                                                     | `typography.greeting.letterSpacing`                                       |
| `--uc-letter-spacing-label-caps` | `0.05em`                                                                      | `typography.label-caps.letterSpacing`                                     |

### Espacamento (`spacing`)

| Token                     | Valor  | Papel                                                                                      |
| ------------------------- | ------ | ------------------------------------------------------------------------------------------ |
| `--uc-space-unit`         | `4px`  | `spacing.unit` - unidade base; componha com `calc()`                                       |
| `--uc-space-card-padding` | `18px` | `spacing.card-padding` - padding interno padrao de card (fora do grid de 4px, intencional) |
| `--uc-space-section-gap`  | `20px` | `spacing.section-gap` - separacao entre blocos verticais                                   |
| `--uc-space-page-margin`  | `32px` | `spacing.page-margin` - enquadramento do conteudo                                          |

Nao ha ramp numerico `--uc-space-{n}`: o `DESIGN.md` nao define um. Um consumidor
que precise de um pede via "Ask First".

### Raio (`rounded`)

| Token                 | Valor    | Papel                                                                                 |
| --------------------- | -------- | ------------------------------------------------------------------------------------- |
| `--uc-radius-sm`      | `6px`    | `rounded.sm` - chips e controles pequenos                                             |
| `--uc-radius-default` | `8px`    | `rounded.DEFAULT` - cards e inputs                                                    |
| `--uc-radius-md`      | `12px`   | `rounded.md` - cards e inputs (card generico da 14.2)                                 |
| `--uc-radius-lg`      | `14px`   | `rounded.lg` - container externo de um bloco de app                                   |
| `--uc-radius-full`    | `9999px` | `rounded.full` - pill: badges de comunidade e botoes de acao; nunca cards de conteudo |

### Elevacao

| Token                 | Valor                            | Papel                                                                                   |
| --------------------- | -------------------------------- | --------------------------------------------------------------------------------------- |
| `--uc-shadow-overlay` | `0 8px 24px rgba(0, 0, 0, 0.08)` | `DESIGN.md` prosa "Elevation & Depth" - dropdowns e toasts; valor de partida, ajustavel |

## Verificacao rapida

```bash
cd frontend
npm test          # tokens.spec.ts trava tokens + este README + classes .uc-text-*
npm run build     # styles.scss compila sem erro de Sass
```
