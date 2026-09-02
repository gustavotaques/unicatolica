import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_BASE_URL } from '../../core/config/api.config';
import { Feed } from './feed';

/**
 * Logout não é mais responsabilidade desta tela — ver {@code Shell.sair()} e
 * `layout/shell/shell.spec.ts`. Este componente só carrega e mostra dado da Home.
 */
describe('Feed', () => {
  let fixture: ComponentFixture<Feed>;
  let component: Feed;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('pacext.token', 'token-fake');
    await TestBed.configureTestingModule({
      imports: [Feed],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Feed);
    component = fixture.componentInstance;
    fixture.detectChanges();

    // Carregamento inicial da Home (constructor do componente): usuário, "minhas
    // comunidades" e comunidades abertas em paralelo (forkJoin).
    httpMock.expectOne(`${API_BASE_URL}/usuarios/me`).flush({
      id: 1,
      nome: 'Julia Fontana',
      email: 'julia@catolicasc.edu.br',
      perfil: 'ALUNO',
      curso: 'Engenharia de Software',
    });
    httpMock.expectOne(`${API_BASE_URL}/comunidades/minhas`).flush([]);
    httpMock
      .expectOne(`${API_BASE_URL}/comunidades`)
      .flush({ content: [], page: 0, size: 6, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('cria o componente e mostra a saudação com nome e curso', () => {
    expect(component).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Julia Fontana');
    expect(compiled.textContent).toContain('Engenharia de Software');
  });

  it('mostra estado vazio quando não há comunidades pra descobrir', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Nenhuma comunidade nova pra descobrir agora');
  });
});
