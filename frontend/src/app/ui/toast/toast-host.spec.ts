import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TOAST_DURACAO_MS, ToastService } from './toast.service';
import { UcToastHost } from './toast-host';

describe('UcToastHost', () => {
  let fixture: ComponentFixture<UcToastHost>;
  let service: ToastService;

  beforeEach(async () => {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({ imports: [UcToastHost] }).compileComponents();
    fixture = TestBed.createComponent(UcToastHost);
    service = TestBed.inject(ToastService);
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function wrapper(): HTMLElement {
    return (fixture.nativeElement as HTMLElement).querySelector('[role="status"]') as HTMLElement;
  }

  function itens(): HTMLElement[] {
    return [
      ...(fixture.nativeElement as HTMLElement).querySelectorAll('.uc-toast-host__item'),
    ] as HTMLElement[];
  }

  it('renderiza o wrapper live-region mesmo sem nenhum toast', () => {
    expect(wrapper()).not.toBeNull();
    expect(wrapper().getAttribute('aria-live')).toBe('polite');
    expect(wrapper().getAttribute('aria-atomic')).toBe('false');
    expect(itens()).toHaveLength(0);
  });

  it('renderiza um toast enfileirado com o icone de check e a mensagem', () => {
    service.mostrar('Você entrou em Atlética');
    fixture.detectChanges();

    expect(itens()).toHaveLength(1);
    expect(itens()[0].textContent?.trim()).toBe('Você entrou em Atlética');

    const svg = itens()[0].querySelector('svg');
    expect(svg).not.toBeNull();
    expect(svg!.getAttribute('stroke')).toBe('currentColor');
    expect(svg!.getAttribute('aria-hidden')).toBe('true');
  });

  it('renderiza dois toasts simultaneos quando mostrar() e chamado duas vezes seguidas', () => {
    service.mostrar('Primeira mensagem');
    service.mostrar('Segunda mensagem');
    fixture.detectChanges();

    expect(itens().map((el) => el.textContent?.trim())).toEqual([
      'Primeira mensagem',
      'Segunda mensagem',
    ]);
  });

  it('mostrar("") nao renderiza nenhum item', () => {
    service.mostrar('   ');
    fixture.detectChanges();

    expect(itens()).toHaveLength(0);
  });

  it('remove o item renderizado quando o servico o descarta apos TOAST_DURACAO_MS', () => {
    service.mostrar('Você entrou em Atlética');
    fixture.detectChanges();
    expect(itens()).toHaveLength(1);

    vi.advanceTimersByTime(TOAST_DURACAO_MS);
    fixture.detectChanges();

    expect(itens()).toHaveLength(0);
  });
});

// jsdom cannot read computed styles from an external stylesheet; the fixed
// bottom-right position / card surface / motion half of the I/O matrix is
// pinned to the SCSS source (as Story 14.1's tokens.spec.ts does).
describe('toast-host.scss style contract', () => {
  const dir = dirname(fileURLToPath(import.meta.url));
  const stripComments = (s: string): string =>
    s.replace(/\/\*[\s\S]*?\*\//g, '').replace(/(^|[^:])\/\/.*$/gm, '$1');
  const norm = stripComments(readFileSync(join(dir, 'toast-host.scss'), 'utf8')).replace(
    /\s+/g,
    ' ',
  );

  it('is wired to the component via styleUrl', () => {
    expect(readFileSync(join(dir, 'toast-host.ts'), 'utf8')).toContain(
      "styleUrl: './toast-host.scss'",
    );
  });

  it('is fixed-positioned bottom-right using the page-margin token', () => {
    expect(norm).toContain('position: fixed');
    expect(norm).toContain('right: var(--uc-space-page-margin)');
    expect(norm).toContain('bottom: var(--uc-space-page-margin)');
  });

  it('is a card-like surface with border, radius and overlay shadow tokens', () => {
    expect(norm).toContain('background: var(--uc-color-surface)');
    expect(norm).toContain('border: 1px solid var(--uc-color-border)');
    expect(norm).toContain('border-radius: var(--uc-radius-md)');
    expect(norm).toContain('box-shadow: var(--uc-shadow-overlay)');
  });

  it('uses the green-ok token for the icon colour', () => {
    expect(norm).toContain('color: var(--uc-color-green-ok)');
  });

  it('has an entrance-only fade/slide keyframes at 150ms with a reduced-motion guard', () => {
    expect(norm).toMatch(/@keyframes [a-z-]+\s*\{\s*from\s*\{\s*opacity:\s*0/);
    expect(norm).toMatch(/animation:\s*[a-z-]+\s*150ms\s*ease-out/);
    expect(norm).toMatch(/@media \(prefers-reduced-motion:\s*reduce\)\s*\{[^}]*animation:\s*none/);
  });
});
