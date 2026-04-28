import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TtaipResolucionDetalleComponent } from './ttaip-resolucion-detalle.component';
import { ActivatedRoute } from '@angular/router';

describe('TtaipResolucionDetalleComponent', () => {
  let component: TtaipResolucionDetalleComponent;
  let fixture: ComponentFixture<TtaipResolucionDetalleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TtaipResolucionDetalleComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => 'EXP-TEST-123' } } // Simulamos una URL falsa
          }
        }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TtaipResolucionDetalleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
