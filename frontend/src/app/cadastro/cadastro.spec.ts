import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Cadastro } from './cadastro';

describe('Cadastro', () => {
  let fixture: ComponentFixture<Cadastro>;
  let component: Cadastro;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cadastro],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Cadastro);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function preencherFormularioValido(): void {
    component['form'].setValue({
      nome: 'Ana Silva',
      email: 'ana@catolicasc.edu.br',
      senha: 'senha123',
      curso: 'Engenharia de Software',
      dataNascimento: '2005-01-01',
    });
  }

  it('não envia requisição quando o formulário é inválido', () => {
    component['enviar']();

    httpMock.expectNone('http://localhost:8080/auth/registro');
    expect(component['form'].touched).toBe(true);
  });

  it('envia o cadastro e exibe os dados retornados em caso de sucesso', () => {
    preencherFormularioValido();

    component['enviar']();

    const request = httpMock.expectOne('http://localhost:8080/auth/registro');
    expect(request.request.method).toBe('POST');
    request.flush({
      id: 1,
      nome: 'Ana Silva',
      email: 'ana@catolicasc.edu.br',
      curso: 'Engenharia de Software',
      emailConfirmado: false,
      criadoEm: '2026-08-27T00:00:00Z',
    });

    expect(component['sucesso']()?.email).toBe('ana@catolicasc.edu.br');
    expect(component['erro']()).toBeNull();
  });

  it('exibe a mensagem específica do envelope de erro quando a API rejeita', () => {
    preencherFormularioValido();

    component['enviar']();

    const request = httpMock.expectOne('http://localhost:8080/auth/registro');
    request.flush(
      { error: { code: 'EMAIL_JA_CADASTRADO', message: 'Esse e-mail já tem uma conta. Esqueceu a senha?' } },
      { status: 409, statusText: 'Conflict' },
    );

    expect(component['erro']()).toBe('Esse e-mail já tem uma conta. Esqueceu a senha?');
    expect(component['sucesso']()).toBeNull();
  });
});
