import type { Page } from '@playwright/test';
import { jwtNaoAssinado } from './jwt';

/**
 * Mocks das rotas `/auth/*` via `page.route()`, para as suítes rodarem sem o
 * backend Quarkus. O frontend chama `http://localhost:8080` cross-origin, então
 * toda resposta mockada carrega cabeçalhos CORS e um `OPTIONS` (preflight que o
 * HttpClient dispara por causa do `Content-Type: application/json`) responde
 * `204`.
 */
const CORS = {
  'access-control-allow-origin': '*',
  'access-control-allow-methods': 'GET,POST,OPTIONS',
  'access-control-allow-headers': 'content-type,authorization',
} as const;

/** Envelope de erro padrão da API (AD-5): `{ error: { code, message, details } }`. */
function envelopeErro(code: string, message: string): string {
  return JSON.stringify({ error: { code, message, details: null } });
}

type Padrao = string | ((url: URL) => boolean);

async function rota(page: Page, padrao: Padrao, status: number, body: string): Promise<void> {
  await page.route(padrao, (route) => {
    if (route.request().method() === 'OPTIONS') {
      return route.fulfill({ status: 204, headers: CORS });
    }
    return route.fulfill({
      status,
      headers: { ...CORS, 'content-type': 'application/json' },
      body,
    });
  });
}

/** `POST /auth/login` -> 200 com um JWT não assinado carregando `perfis`. */
export function mockLoginOk(page: Page, perfis: string[] = ['ALUNO']): Promise<void> {
  return rota(page, '**/auth/login', 200, JSON.stringify({ token: jwtNaoAssinado(perfis) }));
}

/** `POST /auth/login` -> 401 (credenciais inválidas). */
export function mockLogin401(page: Page): Promise<void> {
  return rota(
    page,
    '**/auth/login',
    401,
    envelopeErro('CREDENCIAIS_INVALIDAS', 'E-mail ou senha inválidos.'),
  );
}

/** `POST /auth/logout` -> 204 (best-effort; o frontend engole erro de qualquer forma). */
export function mockLogoutOk(page: Page): Promise<void> {
  return rota(page, '**/auth/logout', 204, '');
}

/** `POST /auth/confirmacao-email/{token}` -> 204 (confirmado). */
export function mockConfirmacaoOk(page: Page): Promise<void> {
  return rota(page, '**/auth/confirmacao-email/**', 204, '');
}

/** `POST /auth/confirmacao-email/{token}` -> 422 com mensagem específica. */
export function mockConfirmacaoInvalida(page: Page): Promise<void> {
  return rota(
    page,
    '**/auth/confirmacao-email/**',
    422,
    envelopeErro('TOKEN_INVALIDO', 'Este link de confirmação expirou ou já foi usado.'),
  );
}

/**
 * Mocka os 3 GETs que a Home (`/feed`, Epic 2) dispara ao montar
 * (`usuarios/me`, `comunidades/minhas`, `comunidades` com `tipo=ABERTA`), pra
 * navegar até lá sem depender de backend nem poluir o console com
 * `ERR_CONNECTION_REFUSED`. Conteúdo da Home é fora de escopo desta suíte
 * (Story 14.3/14.5 cobrem só a casca) - os defaults só existem pra a página
 * assentar num estado limpo por trás do chrome (sidebar/topbar) que os testes
 * de shell realmente verificam.
 */
export async function mockFeedOk(page: Page): Promise<void> {
  const usuario = {
    id: 1,
    nome: 'Usuário Teste',
    email: 'usuario.teste@catolicasc.edu.br',
    perfil: 'ALUNO',
    curso: null,
  };
  const paginaVazia = { content: [], page: 0, size: 6, totalElements: 0, totalPages: 0 };

  await rota(page, (url) => url.pathname === '/usuarios/me', 200, JSON.stringify(usuario));
  await rota(page, (url) => url.pathname === '/comunidades/minhas', 200, JSON.stringify([]));
  await rota(page, (url) => url.pathname === '/comunidades', 200, JSON.stringify(paginaVazia));
}
