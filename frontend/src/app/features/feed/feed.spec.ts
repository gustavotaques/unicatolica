import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Feed } from './feed';

describe('Feed', () => {
  let fixture: ComponentFixture<Feed>;
  let component: Feed;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Feed],
    }).compileComponents();

    fixture = TestBed.createComponent(Feed);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('cria o componente', () => {
    expect(component).toBeTruthy();
  });
});
