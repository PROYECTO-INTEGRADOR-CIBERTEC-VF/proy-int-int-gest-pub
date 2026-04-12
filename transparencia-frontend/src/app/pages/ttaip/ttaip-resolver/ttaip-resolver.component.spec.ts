import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing'; // 1. Importamos el simulador de rutas
import { TtaipResolverComponent } from './ttaip-resolver.component';

describe('TtaipResolverComponent', () => {
  let component: TtaipResolverComponent;
  let fixture: ComponentFixture<TtaipResolverComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // 2. Añadimos RouterTestingModule a los imports de la prueba
      imports: [TtaipResolverComponent, RouterTestingModule]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TtaipResolverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
