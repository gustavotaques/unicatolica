import { TestBed } from '@angular/core/testing';
import { TOAST_DURACAO_MS, ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('mostrar(mensagem) com texto nao-vazio adiciona um item a toasts()', () => {
    service.mostrar('Você entrou em Atlética');

    const itens = service.toasts();
    expect(itens).toHaveLength(1);
    expect(itens[0].mensagem).toBe('Você entrou em Atlética');
    expect(typeof itens[0].id).toBe('number');
  });

  it('remove o item de toasts() apos o timer atingir TOAST_DURACAO_MS', () => {
    service.mostrar('Você entrou em Atlética');
    expect(service.toasts()).toHaveLength(1);

    vi.advanceTimersByTime(TOAST_DURACAO_MS);

    expect(service.toasts()).toEqual([]);
  });

  it('duas chamadas mostrar() seguidas mantem ambos os itens simultaneamente, cada um com seu proprio timer', () => {
    service.mostrar('Primeira mensagem');
    vi.advanceTimersByTime(TOAST_DURACAO_MS / 2);
    service.mostrar('Segunda mensagem');

    expect(service.toasts().map((item) => item.mensagem)).toEqual([
      'Primeira mensagem',
      'Segunda mensagem',
    ]);

    // O primeiro timer completa (TOAST_DURACAO_MS desde a 1a chamada); o
    // segundo, disparado meio ciclo depois, ainda nao completou o seu.
    vi.advanceTimersByTime(TOAST_DURACAO_MS / 2);
    expect(service.toasts().map((item) => item.mensagem)).toEqual(['Segunda mensagem']);

    vi.advanceTimersByTime(TOAST_DURACAO_MS / 2);
    expect(service.toasts()).toEqual([]);
  });

  it('mostrar("") e um no-op silencioso', () => {
    service.mostrar('');

    expect(service.toasts()).toEqual([]);
  });

  it('mostrar("   ") (so espaco em branco) e um no-op silencioso', () => {
    service.mostrar('   ');

    expect(service.toasts()).toEqual([]);
  });

  it('trima a mensagem antes de guarda-la', () => {
    service.mostrar('  Você entrou em Atlética  ');

    expect(service.toasts()[0].mensagem).toBe('Você entrou em Atlética');
  });
});
