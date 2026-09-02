import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { ComunidadeDetalhe } from './comunidade-detalhe';

describe('ComunidadeDetalhe', () => {
  let fixture: ComponentFixture<ComunidadeDetalhe>;
  let httpMock: HttpTestingController;

  async function montar(id: string): Promise<ComponentFixture<ComunidadeDetalhe>> {
    await TestBed.configureTestingModule({
      imports: [ComunidadeDetalhe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id }) } },
        },
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    const criado = TestBed.createComponent(ComunidadeDetalhe);
    criado.detectChanges();
    fixture = criado;
    return criado;
  }

  afterEach(() => httpMock.verify());

  it('mostra o cabeçalho com nome, tipo e botão Participar pra comunidade aberta sem membro', async () => {
    const f = await montar('27');

    httpMock.expectOne(`${API_BASE_URL}/comunidades/27`).flush({
      id: 27,
      nome: 'Clube de Xadrez',
      descricao: null,
      tipo: 'ABERTA',
      souMembro: false,
      criadoEm: '2026-01-01T00:00:00Z',
    });
    f.detectChanges();

    const compiled = f.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent?.trim()).toBe('Clube de Xadrez');
    expect(compiled.textContent).toContain('Comunidade aberta');
    expect(compiled.querySelector('[uc-button]')?.textContent?.trim()).toBe('Participar');
  });

  it('comunidade de curso com membro: mostra indicador de membro, sem botão de ação', async () => {
    const f = await montar('14');

    httpMock.expectOne(`${API_BASE_URL}/comunidades/14`).flush({
      id: 14,
      nome: 'Engenharia de Software',
      descricao: null,
      tipo: 'CURSO',
      souMembro: true,
      criadoEm: '2026-01-01T00:00:00Z',
    });
    f.detectChanges();

    const compiled = f.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('associação automática');
    expect(compiled.querySelector('uc-member-indicator')).toBeTruthy();
    expect(compiled.querySelector('[uc-button]')).toBeNull();
    expect(compiled.querySelector('.detalhe__botao-sair')).toBeNull();
  });

  it('sai de uma comunidade aberta e atualiza o cabeçalho pra "Participar"', async () => {
    const f = await montar('27');

    httpMock.expectOne(`${API_BASE_URL}/comunidades/27`).flush({
      id: 27,
      nome: 'Clube de Xadrez',
      descricao: null,
      tipo: 'ABERTA',
      souMembro: true,
      criadoEm: '2026-01-01T00:00:00Z',
    });
    f.detectChanges();

    const compiled = f.nativeElement as HTMLElement;
    (compiled.querySelector('.detalhe__botao-sair') as HTMLButtonElement).click();

    httpMock.expectOne(`${API_BASE_URL}/comunidades/27/membros/me`).flush(null);
    httpMock.expectOne(`${API_BASE_URL}/comunidades/minhas`).flush([]);
    f.detectChanges();

    expect(compiled.querySelector('[uc-button]')?.textContent?.trim()).toBe('Participar');
  });

  it('erro ao carregar mostra mensagem amigável', async () => {
    const f = await montar('999');

    httpMock.expectOne(`${API_BASE_URL}/comunidades/999`).flush(
      { error: { code: 'COMUNIDADE_NAO_ENCONTRADA', message: 'Não encontrada.', details: null } },
      { status: 404, statusText: 'Not Found' },
    );
    f.detectChanges();

    expect((f.nativeElement as HTMLElement).querySelector('[role="alert"]')).toBeTruthy();
  });
});
