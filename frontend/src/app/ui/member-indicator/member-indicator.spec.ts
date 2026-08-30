import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UcMemberIndicator } from './member-indicator';

describe('UcMemberIndicator', () => {
  let fixture: ComponentFixture<UcMemberIndicator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [UcMemberIndicator] }).compileComponents();
    fixture = TestBed.createComponent(UcMemberIndicator);
  });

  function text(): string {
    return (fixture.nativeElement as HTMLElement).textContent!.replace(/\s+/g, ' ').trim();
  }

  it('usa a label padrao "Membro"', () => {
    fixture.detectChanges();
    expect(text()).toBe('Membro');
  });

  it('usa a label customizada quando informada', () => {
    fixture.componentRef.setInput('label', 'Voce participa');
    fixture.detectChanges();
    expect(text()).toBe('Voce participa');
  });

  it('cai para "Membro" quando a label recebida e so espaco em branco', () => {
    fixture.componentRef.setInput('label', '   ');
    fixture.detectChanges();
    expect(text()).toBe('Membro');
  });

  it('cai para "Membro" quando a label recebida e uma string vazia', () => {
    fixture.componentRef.setInput('label', '');
    fixture.detectChanges();
    expect(text()).toBe('Membro');
  });

  it('renderiza um icone <svg> inline usando currentColor', () => {
    fixture.detectChanges();
    const svg = (fixture.nativeElement as HTMLElement).querySelector('svg');
    expect(svg).not.toBeNull();
    expect(svg!.getAttribute('stroke')).toBe('currentColor');
  });
});

// jsdom cannot read computed styles from an external stylesheet; the green-ok /
// no-background half of the I/O matrix is pinned to the SCSS source (as Story
// 14.1's tokens.spec.ts does).
describe('member-indicator.scss style contract', () => {
  const dir = dirname(fileURLToPath(import.meta.url));
  // Strip comments before matching so a future prose reword of the header
  // comment (which mentions "background") cannot spuriously pass or fail this.
  const stripComments = (s: string): string =>
    s.replace(/\/\*[\s\S]*?\*\//g, '').replace(/(^|[^:])\/\/.*$/gm, '$1');
  const norm = stripComments(readFileSync(join(dir, 'member-indicator.scss'), 'utf8')).replace(
    /\s+/g,
    ' ',
  );

  it('is wired to the component via styleUrl', () => {
    expect(readFileSync(join(dir, 'member-indicator.ts'), 'utf8')).toContain(
      "styleUrl: './member-indicator.scss'",
    );
  });

  it('is green-ok text with no background', () => {
    expect(norm).toContain('color: var(--uc-color-green-ok)');
    expect(norm).not.toMatch(/(^|[ ;{])background(-color)?\s*:/);
  });
});
