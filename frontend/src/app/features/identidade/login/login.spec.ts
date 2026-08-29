import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let component: Login;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('cria o componente', () => {
    expect(component).toBeTruthy();
  });

  it('renderiza os campos de e-mail e senha', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('input[type="email"]')).toBeTruthy();
    expect(compiled.querySelector('input[type="password"]')).toBeTruthy();
  });

  it('não envia a requisição quando o formulário é inválido', () => {
    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
    submitButton.click();

    httpMock.expectNone('http://localhost:8080/auth/login');
  });

  it('navega para /feed quando o formulário é válido', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const compiled = fixture.nativeElement as HTMLElement;
    const emailInput = compiled.querySelector('input[type="email"]') as HTMLInputElement;
    const senhaInput = compiled.querySelector('input[type="password"]') as HTMLInputElement;

    emailInput.value = 'aluno@catolicasc.edu.br';
    emailInput.dispatchEvent(new Event('input'));
    senhaInput.value = 'Senha123!';
    senhaInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    (compiled.querySelector('button[type="submit"]') as HTMLButtonElement).click();

    httpMock.expectOne('http://localhost:8080/auth/login').flush({ token: 'token-fake' });
    fixture.detectChanges();

    expect(navigateSpy).toHaveBeenCalledWith('/feed');
  });

  it('mostra mensagem de erro quando as credenciais são inválidas', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const emailInput = compiled.querySelector('input[type="email"]') as HTMLInputElement;
    const senhaInput = compiled.querySelector('input[type="password"]') as HTMLInputElement;

    emailInput.value = 'aluno@catolicasc.edu.br';
    emailInput.dispatchEvent(new Event('input'));
    senhaInput.value = 'senha-errada';
    senhaInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    (compiled.querySelector('button[type="submit"]') as HTMLButtonElement).click();

    httpMock.expectOne('http://localhost:8080/auth/login').flush('Credencial inválida', { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    expect(compiled.textContent).toContain('E-mail ou senha inválidos.');
  });

  it('redireciona para /feed no construtor quando já existe um token válido', () => {
    localStorage.setItem('pacext.token', 'token-existente');
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    const localFixture = TestBed.createComponent(Login);
    localFixture.detectChanges();

    expect(navigateSpy).toHaveBeenCalledWith('/feed');
  });
});
