import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { ConfirmarEmail } from './confirmar-email';

describe('ConfirmarEmail', () => {
  let httpMock: HttpTestingController;

  function criarComponente(token: string | null): ComponentFixture<ConfirmarEmail> {
    TestBed.configureTestingModule({
      imports: [ConfirmarEmail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } },
        },
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
    return TestBed.createComponent(ConfirmarEmail);
  }

  afterEach(() => {
    httpMock.verify();
  });

  it('confirma o e-mail automaticamente ao carregar com um token válido', () => {
    const fixture = criarComponente('token-valido');
    const component = fixture.componentInstance;

    httpMock.expectOne('http://localhost:8080/auth/confirmacao-email/token-valido').flush(null);

    expect(component['estado']()).toBe('confirmado');
  });

  it('mostra erro quando o backend rejeita o token', () => {
    const fixture = criarComponente('token-expirado');
    const component = fixture.componentInstance;

    httpMock.expectOne('http://localhost:8080/auth/confirmacao-email/token-expirado').flush(
      { error: { code: 'TOKEN_CONFIRMACAO_EXPIRADO', message: 'Link de confirmação expirado. Solicite um novo.' } },
      { status: 422, statusText: 'Unprocessable Entity' },
    );

    expect(component['estado']()).toBe('erro');
    expect(component['mensagemErro']()).toBe('Link de confirmação expirado. Solicite um novo.');
  });

  it('mostra erro sem chamar a API quando não há token na URL', () => {
    criarComponente(null);

    httpMock.expectNone(() => true);
  });
});
