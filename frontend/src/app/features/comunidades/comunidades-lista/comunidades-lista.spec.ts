import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_BASE_URL } from '../../../core/config/api.config';
import { ToastService } from '../../../ui';
import { ComunidadesLista } from './comunidades-lista';

describe('ComunidadesLista', () => {
  let fixture: ComponentFixture<ComunidadesLista>;
  let component: ComunidadesLista;
  let httpMock: HttpTestingController;

  const pagina = {
    content: [
      { id: 1, nome: 'Engenharia de Software', descricao: null, tipo: 'CURSO', souMembro: null, criadoEm: '2026-01-01T00:00:00Z' },
      { id: 2, nome: 'Xadrez', descricao: 'Clube de xadrez', tipo: 'ABERTA', souMembro: null, criadoEm: '2026-01-02T00:00:00Z' },
    ],
    page: 0,
    size: 12,
    totalElements: 2,
    totalPages: 1,
  };

  beforeEach(async () => {
    localStorage.setItem('pacext.token', 'token-fake');
    await TestBed.configureTestingModule({
      imports: [ComunidadesLista],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ComunidadesLista);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // O componente só faz o GET paginado — "minhas comunidades" é a cache
    // compartilhada do ComunidadesService (normalmente populada pelo Shell), que
    // parte vazia aqui porque não há Shell no teste desta tela isolada.
    httpMock.expectOne((req) => req.url === `${API_BASE_URL}/comunidades`).flush(pagina);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('cria o componente e lista as comunidades carregadas', () => {
    expect(component).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('.lista__card').length).toBe(2);
  });

  it('entra numa comunidade aberta e atualiza o estado sem recarregar tudo', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const botao = compiled.querySelector('.lista__botao-participar') as HTMLButtonElement;

    botao.click();

    httpMock.expectOne(`${API_BASE_URL}/comunidades/2/membros`).flush(null);
    // ComunidadesService.ingressar() recarrega a cache de "minhas comunidades"
    // antes de completar — é esse GET que precisa ser respondido aqui.
    httpMock.expectOne(`${API_BASE_URL}/comunidades/minhas`).flush([{ ...pagina.content[1], souMembro: true }]);
    fixture.detectChanges();

    expect(compiled.querySelector('.lista__botao-sair')).toBeTruthy();
  });

  it('mostra o toast "Você entrou em {comunidade}" ao ingressar (Story 2.4)', () => {
    const toastService = TestBed.inject(ToastService);
    const compiled = fixture.nativeElement as HTMLElement;

    (compiled.querySelector('.lista__botao-participar') as HTMLButtonElement).click();
    httpMock.expectOne(`${API_BASE_URL}/comunidades/2/membros`).flush(null);
    httpMock.expectOne(`${API_BASE_URL}/comunidades/minhas`).flush([{ ...pagina.content[1], souMembro: true }]);

    expect(toastService.toasts().map((t) => t.mensagem)).toContain('Você entrou em Xadrez');
  });
});
