import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { Feed } from './feed';

describe('Feed', () => {
  let fixture: ComponentFixture<Feed>;
  let component: Feed;
  let router: Router;

  beforeEach(async () => {
    localStorage.setItem('pacext.token', 'token-fake');
    await TestBed.configureTestingModule({
      imports: [Feed],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Feed);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('cria o componente', () => {
    expect(component).toBeTruthy();
  });

  it('remove o token e navega para /login ao clicar em Sair', () => {
    const navigateSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    const compiled = fixture.nativeElement as HTMLElement;

    (compiled.querySelector('button') as HTMLButtonElement).click();

    expect(localStorage.getItem('pacext.token')).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith('/login');
  });
});
