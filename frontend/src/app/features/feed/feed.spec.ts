import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { API_BASE_URL } from '../../core/config/api.config';
import { Feed } from './feed';

describe('Feed', () => {
  let fixture: ComponentFixture<Feed>;
  let component: Feed;
  let router: Router;
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
    router = TestBed.inject(Router);
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

  it('cria o componente', () => {
    expect(component).toBeTruthy();
  });

  it('remove o token e navega para /login ao clicar em Sair', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const compiled = fixture.nativeElement as HTMLElement;

    (compiled.querySelector('.home__sair') as HTMLButtonElement).click();

    expect(localStorage.getItem('pacext.token')).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith('/login');

    // Logout dispara POST /auth/logout (Story 1.6, best-effort) — precisa ser respondido
    // pra não sobrar requisição pendente no httpMock.verify() do afterEach.
    httpMock.expectOne(`${API_BASE_URL}/auth/logout`).flush(null);
  });
});
