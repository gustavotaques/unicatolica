import { expect, test } from '@playwright/test';
import { mockConfirmacaoInvalida, mockConfirmacaoOk } from './support/mocks';
import { semToken } from './support/seed';

/**
 * Suíte 1 - telas públicas de auth (Epic 1, restyle 14.7 fora de escopo).
 * Renderização, validação de formulário e foco de teclado visível. Sem
 * backend: `/login` e `/cadastro` não fazem chamada até o submit; a
 * confirmação de e-mail é mockada.
 */

test.beforeEach(async ({ page }) => {
  await semToken(page);
});

test.describe('Login', () => {
  test('renderiza os campos, labels associadas e o botão Entrar', async ({ page }) => {
    await page.goto('/login');

    const email = page.getByLabel('E-mail');
    const senha = page.getByLabel('Senha');
    await expect(email).toHaveAttribute('type', 'email');
    await expect(senha).toHaveAttribute('type', 'password');
    await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible();
  });

  test('submeter vazio não navega e não mostra erro de credencial', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByRole('alert')).toHaveCount(0);
  });

  test('foco de teclado percorre e-mail, senha, Entrar com indicador visível', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('E-mail').focus();

    for (const controle of [
      page.getByLabel('E-mail'),
      page.getByLabel('Senha'),
      page.getByRole('button', { name: 'Entrar' }),
    ]) {
      await expect(controle).toBeFocused();
      const outline = await controle.evaluate((el) => {
        const s = getComputedStyle(el);
        return { style: s.outlineStyle, width: s.outlineWidth };
      });
      expect(outline.style !== 'none' || outline.width !== '0px').toBe(true);
      await page.keyboard.press('Tab');
    }
  });
});

test.describe('Cadastro', () => {
  const CAMPOS = ['Nome completo', 'E-mail institucional', 'Senha', 'Curso', 'Data de nascimento'];

  test('renderiza os cinco campos com label associada e o botão Cadastrar', async ({ page }) => {
    await page.goto('/cadastro');

    for (const rotulo of CAMPOS) {
      await expect(page.getByLabel(rotulo)).toBeVisible();
    }
    await expect(page.getByRole('button', { name: 'Cadastrar' })).toBeVisible();
  });

  test('submeter vazio mostra o erro de cada campo e não navega', async ({ page }) => {
    await page.goto('/cadastro');
    await page.getByRole('button', { name: 'Cadastrar' }).click();

    await expect(page.locator('.campo-erro')).toHaveCount(5);
    await expect(page.getByText('Informe seu nome.')).toBeVisible();
    await expect(page).toHaveURL(/\/cadastro$/);
  });

  test('e-mail inválido dispara a mensagem específica após blur', async ({ page }) => {
    await page.goto('/cadastro');
    await page.getByLabel('E-mail institucional').fill('abc');
    await page.getByLabel('Senha').click(); // tira o foco -> marca como touched

    await expect(page.getByText('Informe um e-mail institucional válido.')).toBeVisible();
  });
});

test.describe('Confirmar e-mail', () => {
  test('sem token na URL mostra link inválido e caminho de volta ao cadastro', async ({ page }) => {
    await page.goto('/confirmar-email');

    await expect(page.getByRole('heading', { name: 'Não foi possível confirmar' })).toBeVisible();
    await expect(page.getByText('Link de confirmação inválido.')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Voltar ao cadastro' })).toHaveAttribute(
      'href',
      '/cadastro',
    );
  });

  test('token válido (mock 204) confirma e leva ao login', async ({ page }) => {
    await mockConfirmacaoOk(page);
    await page.goto('/confirmar-email?token=token-valido');

    await expect(page.getByRole('heading', { name: 'E-mail confirmado!' })).toBeVisible();
    await page.getByRole('link', { name: 'Ir para o login' }).click();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('token rejeitado (mock 422) mostra a mensagem da API', async ({ page }) => {
    await mockConfirmacaoInvalida(page);
    await page.goto('/confirmar-email?token=token-expirado');

    await expect(page.getByRole('heading', { name: 'Não foi possível confirmar' })).toBeVisible();
    await expect(page.getByText('Este link de confirmação expirou ou já foi usado.')).toBeVisible();
  });
});
