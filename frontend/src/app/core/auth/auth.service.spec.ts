import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('envia email e senha para POST /auth/login', () => {
    service.login('aluno@catolicasc.edu.br', 'Senha123!').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'aluno@catolicasc.edu.br', senha: 'Senha123!' });
    req.flush({ token: 'token-fake' });
  });

  it('armazena o token retornado para uso posterior', () => {
    service.login('aluno@catolicasc.edu.br', 'Senha123!').subscribe();

    httpMock.expectOne('http://localhost:8080/auth/login').flush({ token: 'token-fake' });

    expect(service.obterToken()).toBe('token-fake');
  });
});
